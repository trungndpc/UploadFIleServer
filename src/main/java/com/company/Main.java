package com.company;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.company.common.AppConfig;
import com.company.route.UploadFileRoute;

import java.util.concurrent.CompletionStage;

public class Main {
    interface Message {}

    private static final class StartFailed implements Message {
        final Throwable ex;

        public StartFailed(Throwable ex) {
            this.ex = ex;
        }
    }

    private static final class Started implements Message {
        final ServerBinding binding;

        public Started(ServerBinding binding) {
            this.binding = binding;
        }
    }

    private static final class Stop implements Message {}

    public static Behavior<Message> create(String host, Integer port) {
        return Behaviors.setup(ctx -> {
            ActorSystem<Void> system = ctx.getSystem();
            Route routes = new UploadFileRoute(ctx).route();

            CompletionStage<ServerBinding> serverBinding =
                    Http.get(system)
                            .newServerAt(host, port)
                            .bind(routes);

            ctx.pipeToSelf(serverBinding, (binding, failure) -> {
                if (binding != null) return new Started(binding);
                else return new StartFailed(failure);
            });

            return starting(false);
        });
    }

    private static Behavior<Message> starting(boolean wasStopped) {
        return Behaviors.setup(ctx ->
                BehaviorBuilder.<Message>create()
                        .onMessage(StartFailed.class, failed -> {
                            throw new RuntimeException("Server failed to start", failed.ex);
                        })
                        .onMessage(Started.class, msg -> {
                            ctx.getLog().info(
                                    "Server online at http://{}:{}",
                                    msg.binding.localAddress().getAddress(),
                                    msg.binding.localAddress().getPort());

                            if (wasStopped) ctx.getSelf().tell(new Stop());

                            return running(msg.binding);
                        })
                        .onMessage(Stop.class, s -> {
                            return starting(true);
                        })
                        .build());
    }

    private static Behavior<Message> running(ServerBinding binding) {
        return BehaviorBuilder.<Message>create()
                .onMessage(Stop.class, msg -> Behaviors.stopped())
                .onSignal(PostStop.class, msg -> {
                    binding.unbind();
                    return Behaviors.same();
                })
                .build();
    }

    public static void main(String[] args) {
        String name = AppConfig.config.getString("server.name");
        String host = AppConfig.config.getString("server.host");
        int port = AppConfig.config.getInt("server.port");
        ActorSystem.create(
                Main.create(host, port), name);
    }
}
