package core

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.CharsetUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import utils.calculateDelayTime
import java.net.InetSocketAddress
import java.time.LocalDateTime

class NettyTask(
  time: LocalDateTime,
  content: List<String>,
  private val host: String,
  private val port: Int,
) : Task(time, content) {

  override suspend fun run(onfinish: (content: String) -> Unit) {
    withContext(Dispatchers.IO) {
      val baseTime = LocalDateTime.now()
      val delays = calculateDelayTime(baseTime, time)
      println("basetime $baseTime task $time delay $delays")
      delay(delays)
      val group: EventLoopGroup = NioEventLoopGroup()
      try {
        val b = Bootstrap()
        b.group(group)
          .channel(NioSocketChannel::class.java)
          .remoteAddress(InetSocketAddress(host, port))
          .handler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
              ch.pipeline().addLast(
                object : SimpleChannelInboundHandler<ByteBuf>() {
                  override fun channelActive(ctx: ChannelHandlerContext) {
                    println("$time 开始发送")
                    ctx.writeAndFlush(
                      Unpooled.copiedBuffer(
                        content.toString(),
                        CharsetUtil.UTF_8
                      )
                    )
                    onfinish(content.toString())
                  }

                  override fun channelRead0(ctx: ChannelHandlerContext, `in`: ByteBuf) {
                    println(
                      "Client received: " + `in`.toString(CharsetUtil.UTF_8)
                    )
                  }

                  @Deprecated("Deprecated in Java")
                  override fun exceptionCaught(
                    ctx: ChannelHandlerContext,
                    cause: Throwable,
                  ) {
                    cause.printStackTrace()
                    ctx.close()
                  }
                }
              )
            }
          })
        val f = b.connect().sync()
        f.channel().closeFuture().sync()
      } catch (e: InterruptedException) {
        e.printStackTrace()
      } finally {
        try {
          group.shutdownGracefully().sync()
        } catch (e: InterruptedException) {
          e.printStackTrace()
        }
      }
    }
  }
}