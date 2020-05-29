package utils

import UADAB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.AlphaComposite
import java.awt.Image
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

object ImageUtils {
    private val IMAGE_CACHE: MutableMap<String, BufferedImage?> = mutableMapOf()
    private val SCHEDULED_FUTURES: MutableMap<String, MutableList<CompletableFuture<BufferedImage>>> = mutableMapOf()

    fun mergeImages(img1: BufferedImage, img2: BufferedImage) =
        BufferedImage(img1.width, img1.height, BufferedImage.TYPE_INT_ARGB).apply {
            createGraphics().apply {
                drawImage(img1, 0, 0, null)
                composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)
                drawImage(
                    if(img1.width == img2.width && img1.height == img2.height) img2
                    else img2.getScaledInstance(img1.width, img2.height, Image.SCALE_AREA_AVERAGING),
                    0, 0, null)
                dispose()
            }
        }

    fun resize(img: BufferedImage, newW: Int, newH: Int) =
        BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB).apply {
            createGraphics().apply {
                drawImage(
                    img.getScaledInstance(newW, newH, Image.SCALE_AREA_AVERAGING),
                    0, 0, null
                )
                dispose()
            }
        }

    private const val USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36"

    private const val ACCEPT_HEADER =
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"

    fun readImg(url: String): CompletableFuture<BufferedImage> {
        val ret = CompletableFuture<BufferedImage>()

        if (IMAGE_CACHE.containsKey(url)) {
            val cached = IMAGE_CACHE[url]
            // cached == null <=> Image loading in progress
            if (cached == null) SCHEDULED_FUTURES.computeIfAbsent(url) { mutableListOf() }.add(ret)
            else ret.complete(IMAGE_CACHE[url])
            return ret
        }
        UADAB.log.debug("Loading image $url")
        IMAGE_CACHE[url] = null //Placeholder to send only one request
        GlobalScope.launch(Dispatchers.IO) {
            val img = runCatching {
                ImageIO.read(JavaHttpRequestBuilder(url).build().apply {
                    setRequestProperty("user-agent", USER_AGENT)
                    setRequestProperty("accept", ACCEPT_HEADER)
                }.inputStream)
            }.getOrElse { BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB) }
            IMAGE_CACHE[url] = img
            ret.complete(img)
            SCHEDULED_FUTURES[url]?.forEach { it.complete(img) }
            SCHEDULED_FUTURES.remove(url).runCatching { }
            UADAB.log.debug("Image $url loaded")
        }
        return ret
    }
}