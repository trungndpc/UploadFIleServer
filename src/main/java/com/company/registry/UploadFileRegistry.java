package com.company.registry;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.RequestContext;
import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import com.company.common.AppConfig;
import com.company.util.CommonUtil;
import com.company.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


public class UploadFileRegistry extends AbstractBehavior<UploadFileRegistry.Request> {
    private static final String NAME_FIELD = "file";
    private int c = 0;
    public UploadFileRegistry(ActorContext<Request> context) {
        super(context);
    }

    public static Behavior<Request> create() {
        return Behaviors.setup(UploadFileRegistry::new);
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
                .onMessage(UploadRequest.class, this::upload)
                .build();
    }

    private Behavior<Request> upload(UploadRequest uploadRequest) {
        Multipart.FormData formData = uploadRequest.formData;
        HttpRequest request = uploadRequest.requestContext.getRequest();
        formData.getParts()
                .filter(part -> NAME_FIELD.equals(part.getName()))
                .mapAsync(1, part -> {
                    String folder = AppConfig.config.getString("storage.folder");
                    Optional<String> originalFileName = part.getFilename();
                    String filename = FileUtil.genFileName(originalFileName.isPresent() ? originalFileName.get() : "");
                    Path path = Paths.get(folder + "/" + filename);
                    return part.getEntity().getDataBytes()
                            .runWith(FileIO.toPath(path), this.getContext().getSystem())
                            .exceptionally(throwable -> {
                                throwable.printStackTrace();
                                return IOResult.createFailed(0, throwable);
                            })
                            .thenApply(ignore -> {
                                if (ignore.wasSuccessful()) {
                                    return CommonUtil.getFullDomain(request) + "/static/upload/" + filename;
                                }
                                return StringUtils.EMPTY;
                            });
                }).runWith(Sink.head(), this.getContext().getSystem())
                .thenAccept((name) -> {
                    if (StringUtils.isEmpty(name)) {
                        uploadRequest.replyTo.tell(new UploadResponse(null, StatusCodes.EXPECTATION_FAILED));
                    } else {
                        uploadRequest.replyTo.tell(new UploadResponse(name, StatusCodes.OK));
                    }
                });
        return Behaviors.same();
    }

    public interface Request {
    }

    public static class UploadRequest implements Request {
        public final RequestContext requestContext;
        public Multipart.FormData formData;
        public final ActorRef<UploadResponse> replyTo;

        public UploadRequest(RequestContext requestContext, Multipart.FormData formData, ActorRef<UploadResponse> replyTo) {
            this.requestContext = requestContext;
            this.formData = formData;
            this.replyTo = replyTo;
        }
    }

    public interface Response {
    }

    public static class UploadResponse implements Response {
        public final String link;
        public final StatusCode status;

        public UploadResponse(String link, StatusCode status) {
            this.link = link;
            this.status = status;
        }
    }


}
