package core

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.CharsetUtil
import java.net.InetSocketAddress


class EchoServer(private val port: Int) {
  @Throws(Exception::class)
  fun start() {
    val serverHandler = EchoServerHandler()
    val group: EventLoopGroup = NioEventLoopGroup()
    try {
      val b = ServerBootstrap()
      b.group(group)
        .channel(NioServerSocketChannel::class.java)
        .localAddress(InetSocketAddress(port))
        .childHandler(object : ChannelInitializer<SocketChannel>() {
          @Throws(Exception::class)
          public override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(serverHandler)
          }
        })
      val f = b.bind().sync()
      println(EchoServer::class.java.name +
              " started and listening for connections on " + f.channel().localAddress())
      f.channel().closeFuture().sync()
    } finally {
      group.shutdownGracefully().sync()
    }
  }

  companion object {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
      EchoServer(19999).start()
    }
  }
}


@Sharable
class EchoServerHandler : ChannelInboundHandlerAdapter() {
  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    val `in` = msg as ByteBuf
    println(
      "Server received: " + `in`.toString(CharsetUtil.UTF_8))
    ctx.write(`in`)
  }

  @Throws(java.lang.Exception::class)
  override fun channelReadComplete(ctx: ChannelHandlerContext) {
    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
      .addListener(ChannelFutureListener.CLOSE)
  }

  override fun exceptionCaught(
    ctx: ChannelHandlerContext,
    cause: Throwable,
  ) {
    cause.printStackTrace()
    ctx.close()
  }
}
