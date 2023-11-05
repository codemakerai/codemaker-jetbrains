/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client;

import ai.codemaker.sdkv2.client.model.Output;
import ai.codemaker.sdkv2.client.model.CompletionRequest;
import ai.codemaker.sdkv2.client.model.CompletionResponse;
import ai.codemaker.sdkv2.client.model.Input;
import ai.codemaker.sdkv2.client.model.Language;
import ai.codemaker.sdkv2.client.model.Mode;
import ai.codemaker.sdkv2.client.model.Modify;
import ai.codemaker.sdkv2.client.model.Options;
import ai.codemaker.sdkv2.client.model.ProcessRequest;
import ai.codemaker.sdkv2.client.model.ProcessResponse;
import ai.codemaker.service.Codemakerai;
import ai.codemaker.service.EdgeServiceGrpc;
import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DefaultClient implements Client {

    private static final Logger logger = LoggerFactory.getLogger(DefaultClient.class);

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final EdgeServiceGrpc.EdgeServiceBlockingStub client;

    private final Config config;

    public DefaultClient(ApiKeyProvider apiKeyProvider) {
        this(apiKeyProvider, Config.create());
    }

    public DefaultClient(ApiKeyProvider apiKeyProvider, Config config) {
        checkNotNull(apiKeyProvider, "apiKeyProvider");
        checkNotNull(config, "config");

        this.config = config;

        this.client = EdgeServiceGrpc.newBlockingStub(
                ClientInterceptors.intercept(
                        createChannel(config),
                        new AuthenticationInterceptor(apiKeyProvider)
                )
        );
    }

    private static Codemakerai.ProcessOptions createProcessOptions(Options options) {
        final Codemakerai.ProcessOptions.Builder builder = Codemakerai.ProcessOptions
                .newBuilder();

        final Optional<Modify> modify = Optional.ofNullable(options.getModify());
        final Optional<String> codePath = Optional.ofNullable(options.getCodePath());
        final Optional<String> prompt = Optional.ofNullable(options.getPrompt());

        modify.ifPresent(value -> builder.setModify(mapModify(value)));
        codePath.ifPresent(builder::setCodePath);
        prompt.ifPresent(builder::setPrompt);

        builder.setDetectSyntaxErrors(options.isDetectSyntaxErrors());

        return builder.build();
    }

    private static Codemakerai.CompletionOptions createCompletionOptions(Options options) {
        return Codemakerai.CompletionOptions
                .newBuilder()
                .build();
    }

    private static Codemakerai.Mode mapMode(Mode mode) {
        return switch (mode) {
            case CODE -> Codemakerai.Mode.CODE;
            case INLINE_CODE -> Codemakerai.Mode.INLINE_CODE;
            case EDIT_CODE -> Codemakerai.Mode.EDIT_CODE;
            case DOCUMENT -> Codemakerai.Mode.DOCUMENT;
            case FIX_SYNTAX -> Codemakerai.Mode.FIX_SYNTAX;
        };
    }

    private static Codemakerai.Language mapLanguage(Language language) {
        return switch (language) {
            case JAVASCRIPT -> Codemakerai.Language.JAVASCRIPT;
            case JAVA -> Codemakerai.Language.JAVA;
            case CSHARP -> Codemakerai.Language.CSHARP;
            case GO -> Codemakerai.Language.GO;
            case KOTLIN -> Codemakerai.Language.KOTLIN;
            case TYPESCRIPT -> Codemakerai.Language.TYPESCRIPT;
        };
    }

    private static Codemakerai.Modify mapModify(Modify modify) {
        return switch (modify) {
            case NONE -> Codemakerai.Modify.UNMODIFIED;
            case REPLACE -> Codemakerai.Modify.REPLACE;
        };
    }

    private static ManagedChannel createChannel(Config config) {
        return ManagedChannelBuilder.forAddress(config.getEndpoint(), config.getPort())
                .useTransportSecurity()
                .build();
    }

    private static ByteBuffer compress(ByteBuffer input) {
        return ByteBuffer.wrap(compress(input.array(), input.remaining()));
    }

    private static byte[] compress(byte[] bytes, int length) {
        try {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzip = new GZIPOutputStream(output)) {
                gzip.write(bytes, 0, length);
                gzip.finish();
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress input.", e);
        }
    }

    private static ByteBuffer decompress(ByteBuffer input) {
        return ByteBuffer.wrap(decompress(input.array(), input.remaining()));
    }

    public static byte[] decompress(byte[] bytes, int length) {
        try {
            final ByteArrayInputStream input = new ByteArrayInputStream(bytes, 0, length);
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (final GZIPInputStream gzip = new GZIPInputStream(input)) {
                final byte[] buffer = new byte[1024];
                int len;
                while ((len = gzip.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                }
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress input.", e);
        }
    }

    private static String checksum(ByteBuffer content) {
        return Hashing.sha256()
                .hashBytes(content.duplicate())
                .toString();
    }

    @Override
    public CompletionResponse completion(CompletionRequest request) {
        final Codemakerai.CompletionRequest processRequest = createCompletionRequest(request);

        final Codemakerai.CompletionResponse completionResponse = doCompletion(processRequest);

        return createCompletionResponse(completionResponse);
    }

    @Override
    public ProcessResponse process(ProcessRequest request) {
        final Codemakerai.ProcessRequest processRequest = createProcessRequest(request);

        final Codemakerai.ProcessResponse processResponse = doProcess(processRequest);

        return createProcessResponse(processResponse);
    }

    private Codemakerai.CompletionRequest createCompletionRequest(CompletionRequest request) {
        final Codemakerai.Input input = createInput(request.getInput());

        return Codemakerai.CompletionRequest.newBuilder()
                .setLanguage(mapLanguage(request.getLanguage()))
                .setInput(input)
                .setOptions(createCompletionOptions(request.getOptions()))
                .build();
    }

    private Codemakerai.CompletionResponse doCompletion(Codemakerai.CompletionRequest completionRequest) {
        try {
            return client.completion(completionRequest);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private CompletionResponse createCompletionResponse(Codemakerai.CompletionResponse completionResponse) {
        final Codemakerai.Source content = completionResponse.getOutput().getSource();
        final String output = decodeOutput(content);

        return new CompletionResponse(new Output(output));
    }

    private Codemakerai.ProcessRequest createProcessRequest(ProcessRequest request) {
        final Codemakerai.Input input = createInput(request.getInput());

        return Codemakerai.ProcessRequest.newBuilder()
                .setMode(mapMode(request.getMode()))
                .setLanguage(mapLanguage(request.getLanguage()))
                .setInput(input)
                .setOptions(createProcessOptions(request.getOptions()))
                .build();
    }

    private Codemakerai.ProcessResponse doProcess(Codemakerai.ProcessRequest processRequest) {
        try {
            return client.process(processRequest);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private ProcessResponse createProcessResponse(Codemakerai.ProcessResponse processResponse) {
        final Codemakerai.Source content = processResponse.getOutput().getSource();
        final String output = decodeOutput(content);

        return new ProcessResponse(new Output(output));
    }

    private Codemakerai.Input createInput(Input request) {
        return Codemakerai.Input.newBuilder()
                .setSource(encodeInput(request))
                .build();
    }

    private String decodeOutput(Codemakerai.Source source) {
        final ByteString content = source.getContent();
        ByteBuffer output = ByteBuffer.allocate(content.size());
        content.copyTo(output);
        output.flip();

        if (source.getEncoding() == Codemakerai.Encoding.GZIP) {
            output = decompress(output);
        }

        return DEFAULT_CHARSET.decode(output).toString();
    }

    private Codemakerai.Source.Builder encodeInput(Input input) {
        Codemakerai.Encoding encoding = Codemakerai.Encoding.NONE;
        ByteBuffer content = DEFAULT_CHARSET.encode(input.getSource());
        final String checksum = checksum(content);

        if (config.isEnableCompression() &&
                content.remaining() >= config.getMinimumCompressionPayloadSize()) {
            encoding = Codemakerai.Encoding.GZIP;
            content = compress(content);
        }

        return Codemakerai.Source.newBuilder()
                .setContent(ByteString.copyFrom(content))
                .setEncoding(encoding)
                .setChecksum(checksum);
    }
}
