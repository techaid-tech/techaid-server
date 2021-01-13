package ju.ma.controllers

import javax.servlet.http.HttpServletResponse
import ju.ma.app.KitRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.server.ResponseStatusException

@Controller
class ImageController(private val kits: KitRepository) {
    @RequestMapping(
        value = ["/kits/{deviceId}/images/{imageId}"],
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun image(
        @PathVariable deviceId: Long,
        @PathVariable imageId: String,
        response: HttpServletResponse
    ): ResponseEntity<ByteArray> {
        // val kit = kits.findById(deviceId).toNullable() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device with id: $deviceId")
        // val image = kit.images?.images?.firstOrNull { it.id == imageId }
        //     ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find image with id: $imageId")
        // val (_, ext, img) = Regex("data:([^;]+);base64,(.+)").matchEntire(image.image)?.groupValues
        //     ?: listOf("", "image/png", image.image)
        // return ResponseEntity.ok(Base64.getDecoder().decode(img))
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device with id: $deviceId")
    }
}
