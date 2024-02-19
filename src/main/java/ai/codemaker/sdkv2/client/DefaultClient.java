/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client;

import ai.codemaker.sdkv2.client.model.AssistantCodeCompletionRequest;
import ai.codemaker.sdkv2.client.model.AssistantCodeCompletionResponse;
import ai.codemaker.sdkv2.client.model.AssistantCompletionRequest;
import ai.codemaker.sdkv2.client.model.AssistantCompletionResponse;
import ai.codemaker.sdkv2.client.model.CompletionRequest;
import ai.codemaker.sdkv2.client.model.CompletionResponse;
import ai.codemaker.sdkv2.client.model.CreateContextRequest;
import ai.codemaker.sdkv2.client.model.CreateContextResponse;
import ai.codemaker.sdkv2.client.model.DiscoverContextRequest;
import ai.codemaker.sdkv2.client.model.DiscoverContextResponse;
import ai.codemaker.sdkv2.client.model.Input;
import ai.codemaker.sdkv2.client.model.Language;
import ai.codemaker.sdkv2.client.model.Mode;
import ai.codemaker.sdkv2.client.model.Modify;
import ai.codemaker.sdkv2.client.model.Options;
import ai.codemaker.sdkv2.client.model.Output;
import ai.codemaker.sdkv2.client.model.PredictRequest;
import ai.codemaker.sdkv2.client.model.PredictResponse;
import ai.codemaker.sdkv2.client.model.ProcessRequest;
import ai.codemaker.sdkv2.client.model.ProcessResponse;
import ai.codemaker.sdkv2.client.model.RegisterContextRequest;
import ai.codemaker.sdkv2.client.model.RegisterContextResponse;
import ai.codemaker.sdkv2.client.model.RequiredContext;
import ai.codemaker.service.CodemakerServiceGrpc;
import ai.codemaker.service.Codemakerai;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DefaultClient implements Client {

    private static final Logger logger = LoggerFactory.getLogger(DefaultClient.class);

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 120000;

    private final CodemakerServiceGrpc.CodemakerServiceBlockingStub client;

    private final Config config;

    public DefaultClient(ApiKeyProvider apiKeyProvider) {
        this(apiKeyProvider, Config.create());
    }

    public DefaultClient(ApiKeyProvider apiKeyProvider, Config config) {
        checkNotNull(apiKeyProvider, "apiKeyProvider");
        checkNotNull(config, "config");

        this.config = config;

        this.client = CodemakerServiceGrpc.newBlockingStub(
                ClientInterceptors.intercept(
                        createChannel(config),
                        new AuthenticationInterceptor(apiKeyProvider)
                )
        );
    }

    @Override
    public AssistantCompletionResponse assistantCompletion(AssistantCompletionRequest request) {
        final Codemakerai.AssistantCompletionRequest assistantCompletionRequest = createAssistantCompletionRequest(request);

        final Codemakerai.AssistantCompletionResponse assistantCompletionResponse = doAssistantCompletion(assistantCompletionRequest);

        return createAssistantCompletionResponse(assistantCompletionResponse);
    }

    @Override
    public AssistantCodeCompletionResponse assistantCodeCompletion(AssistantCodeCompletionRequest request) {
        final Codemakerai.AssistantCodeCompletionRequest assistantCodeCompletionRequest = createAssistantCodeCompletionRequest(request);

        final Codemakerai.AssistantCodeCompletionResponse assistantCodeCompletionResponse = doAssistantCodeCompletion(assistantCodeCompletionRequest);

        return createAssistantCodeCompletionResponse(assistantCodeCompletionResponse);
    }

    @Override
    public CompletionResponse completion(CompletionRequest request) {
        final Codemakerai.CompletionRequest completionRequest = createCompletionRequest(request);

        final Codemakerai.CompletionResponse completionResponse = doCompletion(completionRequest);

        return createCompletionResponse(completionResponse);
    }

    @Override
    public ProcessResponse process(ProcessRequest request) {
        final Codemakerai.ProcessRequest processRequest = createProcessRequest(request);

        final Codemakerai.ProcessResponse processResponse = doProcess(processRequest);

        return createProcessResponse(processResponse);
    }

    @Override
    public PredictResponse predict(PredictRequest request) {
        final Codemakerai.PredictRequest predictRequest = createPredictRequest(request);

        final Codemakerai.PredictResponse predictResponse = doPredict(predictRequest);

        return createPredictResponse(predictResponse);
    }

    @Override
    public DiscoverContextResponse discoverContext(DiscoverContextRequest request) {
        final Codemakerai.DiscoverSourceContextRequest discoverContextRequest = createDiscoverContextRequest(request);

        final Codemakerai.DiscoverSourceContextResponse discoverContextResponse = doDiscoverContext(discoverContextRequest);

        return createDiscoverContextResponse(discoverContextResponse);
    }

    @Override
    public CreateContextResponse createContext(CreateContextRequest request) {
        final Codemakerai.CreateSourceContextRequest createContextRequest = createCreateContextRequest(request);

        final Codemakerai.CreateSourceContextResponse createContextResponse = doCreateContext(createContextRequest);

        return createCreateContextResponse(createContextResponse);
    }

    @Override
    public RegisterContextResponse registerContext(RegisterContextRequest request) {
        final Codemakerai.RegisterSourceContextRequest registerContextRequest = createRegisterContextRequest(request);

        final Codemakerai.RegisterSourceContextResponse registerContextResponse = doRegisterContext(registerContextRequest);

        return createRegisterContextResponse(registerContextResponse);
    }

    private Codemakerai.DiscoverSourceContextResponse doDiscoverContext(Codemakerai.DiscoverSourceContextRequest request) {
        try {
            return client().discoverContext(request);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private Codemakerai.CreateSourceContextResponse doCreateContext(Codemakerai.CreateSourceContextRequest request) {
        try {
            return client().createContext(request);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private Codemakerai.RegisterSourceContextResponse doRegisterContext(Codemakerai.RegisterSourceContextRequest request) {
        try {
            return client().registerContext(request);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private Codemakerai.AssistantCompletionResponse doAssistantCompletion(Codemakerai.AssistantCompletionRequest request) {
        try {
            return client().assistantCompletion(request);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private Codemakerai.AssistantCodeCompletionResponse doAssistantCodeCompletion(Codemakerai.AssistantCodeCompletionRequest request) {
        try {
            return client().assistantCodeCompletion(request);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private Codemakerai.CompletionResponse doCompletion(Codemakerai.CompletionRequest request) {
        try {
            return client().completion(request);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private Codemakerai.ProcessResponse doProcess(Codemakerai.ProcessRequest request) {
        try {
            return client().process(request);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private Codemakerai.PredictResponse doPredict(Codemakerai.PredictRequest request) {
        try {
            return client().predict(request);
        } catch (StatusRuntimeException e) {
            logger.error("Error calling service {} {}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                throw new UnauthorizedException("Unauthorized request.");
            }
            throw e;
        }
    }

    private CodemakerServiceGrpc.CodemakerServiceBlockingStub client() {
        return client.withDeadlineAfter(DEFAULT_TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    private Codemakerai.DiscoverSourceContextRequest createDiscoverContextRequest(DiscoverContextRequest request) {
        final Codemakerai.Input input = createInput(request.getContext().getInput());

        return Codemakerai.DiscoverSourceContextRequest.newBuilder()
                .setContext(Codemakerai.SourceContext.newBuilder()
                        .setLanguage(mapLanguage(request.getContext().getLanguage()))
                        .setInput(input)
                        .setMetadata(Codemakerai.Metadata.newBuilder()
                                .setPath(request.getContext().getPath())
                                .build())
                        .build())
                .build();
    }

    private DiscoverContextResponse createDiscoverContextResponse(Codemakerai.DiscoverSourceContextResponse response) {
        final Collection<RequiredContext> requiredContexts = response.getContextsList().stream()
                .map(this::mapRequiredContext)
                .toList();
        return new DiscoverContextResponse(requiredContexts, response.getRequiresProcessing());
    }

    private Codemakerai.CreateSourceContextRequest createCreateContextRequest(CreateContextRequest request) {
        return Codemakerai.CreateSourceContextRequest.newBuilder().build();
    }

    private CreateContextResponse createCreateContextResponse(Codemakerai.CreateSourceContextResponse response) {
        return new CreateContextResponse(response.getId());
    }

    private Codemakerai.RegisterSourceContextRequest createRegisterContextRequest(RegisterContextRequest request) {
        final List<Codemakerai.SourceContext> sourceContexts = request.getContexts().stream()
                .map(context -> {
                            final Codemakerai.Input input = createInput(context.getInput());
                            return Codemakerai.SourceContext.newBuilder()
                                    .setLanguage(mapLanguage(context.getLanguage()))
                                    .setInput(input)
                                    .setMetadata(Codemakerai.Metadata.newBuilder()
                                            .setPath(context.getPath())
                                            .build())
                                    .build();
                        }
                )
                .toList();
        return Codemakerai.RegisterSourceContextRequest.newBuilder()
                .setId(request.getId())
                .addAllSourceContexts(sourceContexts)
                .build();
    }

    private RegisterContextResponse createRegisterContextResponse(Codemakerai.RegisterSourceContextResponse response) {
        return new RegisterContextResponse();
    }

    private Codemakerai.AssistantCompletionRequest createAssistantCompletionRequest(AssistantCompletionRequest request) {
        return Codemakerai.AssistantCompletionRequest.newBuilder()
                .setMessage(request.getMessage())
                .build();
    }

    private AssistantCompletionResponse createAssistantCompletionResponse(Codemakerai.AssistantCompletionResponse response) {
        return new AssistantCompletionResponse(response.getMessage());
    }

    private Codemakerai.AssistantCodeCompletionRequest createAssistantCodeCompletionRequest(AssistantCodeCompletionRequest request) {
        final Codemakerai.Input input = createInput(request.getInput());

        return Codemakerai.AssistantCodeCompletionRequest.newBuilder()
                .setMessage(request.getMessage())
                .setLanguage(mapLanguage(request.getLanguage()))
                .setInput(input)
                .setOptions(createAssistantCodeCompletionOptions(request.getOptions()))
                .build();
    }

    private AssistantCodeCompletionResponse createAssistantCodeCompletionResponse(Codemakerai.AssistantCodeCompletionResponse response) {
        final Codemakerai.Source content = response.getOutput().getSource();
        final String output = decodeOutput(content);

        return new AssistantCodeCompletionResponse(response.getMessage(), new Output(output));
    }

    private Codemakerai.CompletionRequest createCompletionRequest(CompletionRequest request) {
        final Codemakerai.Input input = createInput(request.getInput());

        return Codemakerai.CompletionRequest.newBuilder()
                .setLanguage(mapLanguage(request.getLanguage()))
                .setInput(input)
                .setOptions(createCompletionOptions(request.getOptions()))
                .build();
    }

    private CompletionResponse createCompletionResponse(Codemakerai.CompletionResponse response) {
        final Codemakerai.Source content = response.getOutput().getSource();
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

    private ProcessResponse createProcessResponse(Codemakerai.ProcessResponse response) {
        final Codemakerai.Source content = response.getOutput().getSource();
        final String output = decodeOutput(content);

        return new ProcessResponse(new Output(output));
    }

    private Codemakerai.PredictRequest createPredictRequest(PredictRequest request) {
        final Codemakerai.Input input = createInput(request.getInput());

        return Codemakerai.PredictRequest.newBuilder()
                .setLanguage(mapLanguage(request.getLanguage()))
                .setInput(input)
                .build();
    }

    private Codemakerai.Input createInput(Input request) {
        return Codemakerai.Input.newBuilder()
                .setSource(encodeInput(request))
                .build();
    }

    private RequiredContext mapRequiredContext(Codemakerai.RequiredSourceContext requiredContext) {
        return new RequiredContext(requiredContext.getPath());
    }

    private PredictResponse createPredictResponse(Codemakerai.PredictResponse response) {
        return new PredictResponse();
    }

    private static Codemakerai.ProcessOptions createProcessOptions(Options options) {
        final Codemakerai.ProcessOptions.Builder builder = Codemakerai.ProcessOptions
                .newBuilder();

        final Optional<Modify> modify = Optional.ofNullable(options.getModify());
        final Optional<String> codePath = Optional.ofNullable(options.getCodePath());
        final Optional<String> prompt = Optional.ofNullable(options.getPrompt());
        final Optional<String> contextId = Optional.ofNullable(options.getContextId());

        modify.ifPresent(value -> builder.setModify(mapModify(value)));
        codePath.ifPresent(builder::setCodePath);
        prompt.ifPresent(builder::setPrompt);
        contextId.ifPresent(builder::setContextId);

        builder.setDetectSyntaxErrors(options.isDetectSyntaxErrors());

        return builder.build();
    }

    private static Codemakerai.CompletionOptions createCompletionOptions(Options options) {
        // TODO map remaining attributes
        return Codemakerai.CompletionOptions
                .newBuilder()
                .setCodePath(options.getCodePath())
                .setAllowMultiLineAutocomplete(options.isAllowMultiLineAutocomplete())
                .build();
    }

    private Codemakerai.AssistantCodeCompletionOptions createAssistantCodeCompletionOptions(Options options) {
        final Codemakerai.AssistantCodeCompletionOptions.Builder builder = Codemakerai.AssistantCodeCompletionOptions.newBuilder();

        final Optional<String> contextId = Optional.ofNullable(options.getContextId());

        contextId.ifPresent(builder::setContextId);

        return builder.build();
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
            case C -> Codemakerai.Language.C;
            case CPP -> Codemakerai.Language.CPP;
            case PHP -> Codemakerai.Language.PHP;
            case JAVASCRIPT -> Codemakerai.Language.JAVASCRIPT;
            case JAVA -> Codemakerai.Language.JAVA;
            case CSHARP -> Codemakerai.Language.CSHARP;
            case GO -> Codemakerai.Language.GO;
            case KOTLIN -> Codemakerai.Language.KOTLIN;
            case TYPESCRIPT -> Codemakerai.Language.TYPESCRIPT;
            case RUST -> Codemakerai.Language.RUST;
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
}
