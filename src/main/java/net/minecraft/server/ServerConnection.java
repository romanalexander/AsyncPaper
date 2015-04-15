package net.minecraft.server;

import net.minecraft.util.com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.util.io.netty.bootstrap.ServerBootstrap;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.nio.NioEventLoopGroup;
import net.minecraft.util.io.netty.channel.socket.nio.NioServerSocketChannel;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.github.paperspigot.NamedThreadFactory;
import org.github.paperspigot.PaperPhaser;
import org.github.paperspigot.PaperPhaserProvider;
import org.github.paperspigot.PaperSpigotConfig;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerConnection {

    private static final Logger b = LogManager.getLogger();
    private static final NioEventLoopGroup c = new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty IO #%d").setDaemon(true).build());
    private final MinecraftServer d;
    public volatile boolean a;
    private final List e = Collections.synchronizedList(new ArrayList());
    private final List f = Collections.synchronizedList(new ArrayList());

    public ServerConnection(MinecraftServer minecraftserver) {
        this.d = minecraftserver;
        this.a = true;
    }

    public void a(InetAddress inetaddress, int i) {
        List list = this.e;

        synchronized (this.e) {
            this.e.add(((ServerBootstrap) ((ServerBootstrap) (new ServerBootstrap()).channel(NioServerSocketChannel.class)).childHandler(new ServerConnectionChannel(this)).group(c).localAddress(inetaddress, i)).bind().syncUninterruptibly());
        }
    }

    public void b() {
        this.a = false;
        Iterator iterator = this.e.iterator();

        while (iterator.hasNext()) {
            ChannelFuture channelfuture = (ChannelFuture) iterator.next();

            channelfuture.channel().close().syncUninterruptibly();
        }
    }

    private ThreadPoolExecutor connectionHandlerService = new ThreadPoolExecutor(PaperSpigotConfig.connectionHandlerThreads, PaperSpigotConfig.connectionHandlerThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("connection-handler-worker"));

    public void c() {
        List list = this.f;
        MinecraftServer.ignoreAsyncModifications = true;
        synchronized (this.f) {
            // Spigot Start
            // This prevents players from 'gaming' the server, and strategically relogging to increase their position in the tick order
            if (org.spigotmc.SpigotConfig.playerShuffle > 0 && MinecraftServer.currentTick % org.spigotmc.SpigotConfig.playerShuffle == 0) {
                Collections.shuffle(this.f);
            }
            // Spigot End
            Iterator iterator = this.f.iterator();

            final PaperPhaserProvider phaserProvider = new PaperPhaserProvider();
            while (iterator.hasNext()) {
                final NetworkManager networkmanager = (NetworkManager) iterator.next();

                if (!networkmanager.isConnected()) {
                    // Spigot Start
                    // Fix a race condition where a NetworkManager could be unregistered just before connection.
                    if (networkmanager.preparing) continue;
                    // Spigot End
                    iterator.remove();
                    if (networkmanager.f() != null) {
                        networkmanager.getPacketListener().a(networkmanager.f());
                    } else if (networkmanager.getPacketListener() != null) {
                        networkmanager.getPacketListener().a(new ChatComponentText("Disconnected"));
                    }
                } else {
                    final PaperPhaser phaser = phaserProvider.getAndRegister();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                networkmanager.a();
                            } catch (Exception exception) {
                                if (networkmanager.c()) {
                                    CrashReport crashreport = CrashReport.a(exception, "Ticking memory connection");
                                    CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Ticking connection");

                                    crashreportsystemdetails.a("Connection", (Callable) (new CrashReportServerConnection(ServerConnection.this, networkmanager)));
                                    throw new ReportedException(crashreport);
                                }

                                b.warn("Failed to handle packet for " + networkmanager.getSocketAddress(), exception);
                                ChatComponentText chatcomponenttext = new ChatComponentText("Internal server error");

                                networkmanager.handle(new PacketPlayOutKickDisconnect(chatcomponenttext), new GenericFutureListener[]{new ServerConnectionFuture(ServerConnection.this, networkmanager, chatcomponenttext)});
                                networkmanager.g();
                            } finally {
                                phaser.arrive();
                            }
                        }
                    };
                    if(PaperSpigotConfig.connectionHandlerThreads > 1) {
                        connectionHandlerService.submit(runnable);
                    } else {
                        runnable.run();
                    }
                }
            }
            phaserProvider.await();
            MinecraftServer.ignoreAsyncModifications = false;
        }
    }

    public MinecraftServer d() {
        return this.d;
    }

    static List a(ServerConnection serverconnection) {
        return serverconnection.f;
    }

    static MinecraftServer b(ServerConnection serverconnection) {
        return serverconnection.d;
    }
}
