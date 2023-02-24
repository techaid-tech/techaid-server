package ju.ma.controllers

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletWebRequest

/**
 *
 * This is a custom error handler, it renders a pretty error page when the server
 * encounters an error. Depending on the type of request, it will either render json
 * or html.
 *
 */
@RestController
@ConditionalOnWebApplication
class CustomErrorController : ErrorController {
    /**
     * The name of this controllers instance
     */
    @Value("\${spring.application.name:\${APP_NAME:}}")
    private val application: String = ""

    /**
     * The associated error attributes from an exception
     */
    @Autowired
    private lateinit var errorAttributes: ErrorAttributes

    /**
     * Returns the error object as a JSON element
     */
    @RequestMapping(value = ["/error"], produces = ["application/json"])
    fun errorJSON(request: HttpServletRequest, response: HttpServletResponse): CustomErrorModel {
        // Appropriate HTTP response code (e.g. 404 or 500) is automatically set by Spring.
        // Here we just define response body.
        return CustomErrorModel.from(response.status, this.application, getErrorAttributes(request))
    }

    /**
     * Returns the error object as a HTML page
     */
    @RequestMapping(value = ["/error"], produces = ["text/html"])
    fun errorText(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<String> {
        val message =
            CustomErrorModel.from(response.status, this.application, getErrorAttributes(request)).toString()
        return ResponseEntity(message, HttpStatus.valueOf(response.status))
    }

    /**
     * Extracts the error attributes from the http request. Includes the error stack trace
     * if requested
     */
    private fun getErrorAttributes(request: HttpServletRequest): Map<String, Any> {
        val requestAttributes = ServletWebRequest(request)
        return errorAttributes.getErrorAttributes(requestAttributes, ErrorAttributeOptions.defaults())
    }
}

/**
 * A class to represent a http error within the controllers
 */
class CustomErrorModel {

    companion object {
        /**
         * Creates a custom error model from the provided attributes
         */
        fun from(status: Int, application: String, errorAttributes: Map<String, Any>): CustomErrorModel {
            val model = CustomErrorModel()
            model.status = status
            model.application = application
            model.error = errorAttributes["error"] as String
            model.message = errorAttributes["message"] as String
            model.timeStamp = errorAttributes["timestamp"].toString()
            model.trace = errorAttributes["trace"] as String?
            return model
        }
    }

    /**
     * Description of the http error that occurred
     */
    var error: String = ""
    /**
     * The http error code
     */
    var status: Int = 0
    /**
     * Message from the exception if any was raised
     */
    var message: String = ""
    /**
     * The time the error was raised
     */
    var timeStamp: String = ""
    /**
     * The name of the application. Used to identify this instance
     */
    var application: String = ""
    /**
     * The full stacktrace of the exception
     */
    private var trace: String? = null

    /**
     * Returns the HTML representation of the error model
     */
    override fun toString(): String {
        var stackTrace = ""
        if (this.trace != null && !this.trace!!.isEmpty()) {
            stackTrace = "<p><pre>$trace</pre></p>"
        }

        return "<!DOCTYPE html>" +
            "<html><head>" +
            "    <meta http-equiv=\"Content-type\" content=\"text/html; charset=UTF-8\">" +
            "    <meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'none'; style-src 'unsafe-inline'; img-src data:; connect-src 'self'\">" +
            "    <title>Application Error &middot; " + application + "</title>" +
            "    <style type=\"text/css\" media=\"screen\">" +
            "      body {" +
            "        background-color: #f1f1f1;" +
            "        margin: 0;" +
            "        font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif;" +
            "      }" +
            "      .container { margin: 50px auto 40px auto; width: 100%; text-align: center; }" +
            "      a { color: #4183c4; text-decoration: none; }" +
            "      a:hover { text-decoration: underline; }" +
            "      h1 { width: 100%; position:relative; letter-spacing: -1px; line-height: 60px; font-size: 60px; font-weight: 100; margin: 0px 0 50px 0; text-shadow: 0 1px 0 #fff; }" +
            "      p { color: rgba(0, 0, 0, 0.5); margin: 20px 0; line-height: 1.6; padding: 0px 20px;}" +
            "      ul { list-style: none; margin: 25px 0; padding: 0; }" +
            "      li { display: table-cell; font-weight: bold; width: 1%; }" +
            "      .logo { display: inline-block; margin-top: 35px; }" +
            "      .logo-img-2x { display: none; }" +
            "      @media" +
            "      only screen and (-webkit-min-device-pixel-ratio: 2)," +
            "      only screen and (   min--moz-device-pixel-ratio: 2)," +
            "      only screen and (     -o-min-device-pixel-ratio: 2/1)," +
            "      only screen and (        min-device-pixel-ratio: 2)," +
            "      only screen and (                min-resolution: 192dpi)," +
            "      only screen and (                min-resolution: 2dppx) {" +
            "        .logo-img-1x { display: none; }" +
            "        .logo-img-2x { display: inline-block; }" +
            "      }" +
            "      #suggestions {" +
            "        margin-top: 35px;" +
            "        color: #ccc;" +
            "      }" +
            "      #suggestions a {" +
            "        color: #666666;" +
            "        font-weight: 200;" +
            "        font-size: 14px;" +
            "        margin: 0 10px;" +
            "      }" +
            "    </style>" +
            "  </head>" +
            "  <body cz-shortcut-listen=\"true\">" +
            "    <div class=\"container\">" +
            "      <h1>" + status + "</h1>" +
            "      <p><small>" + timeStamp + "</small></p>" +
            "      <p> <strong>" + error + "</strong></p>" +
            "      <p>" + message + "</p>" + stackTrace +
            "      <a href=\"#\" onClick=\"window.location.reload(true)\" title=\"Reload Page\" class=\"logo logo-img-1x\">" +
            "        <img title=\"\" alt=\"\" src=\"data:image/png;base64,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\" height=\"32\" width=\"32\">" +
            "      </a>" +
            "      <a href=\"#\" class=\"logo logo-img-2x\" title=\"Reload Page\" onClick=\"window.location.reload(true)\">" +
            "        <img title=\"\" alt=\"\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAQAAAAEACAYAAABccqhmAAAbDUlEQVR4nO3de5AkVZ0v8BrmwWOAGRB5Dwsq6j4UWOAKAd5bi6H7h8suKCXrH+7TFQEbaGgbRcCz0tXVlVV5ft/vtLSkokKwK2sbrgui4K4YrATg4oKsu0KAvN+uCjMjL5lh6v5hErd3LtOZVV1Vp6ry+4moiImYiJlvnjx58uTJc06WSiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIFNT09/TqSR5rZKQDOI9kwsy+TvBbArWZ2L8nHSf6c5EYAL5J8heQr6Z83pn/3uJndC+BWktem/0YDwHkA3k/ySOfcnqGPV6SQoijaF8C70wvySpJ3kty0fv36Vj9/aSNyB8krzOxcAO9uNBp7hy4fkZFRLpdXeO+PInk2gHkAT/T7Qu+gYXic5FcBnEXyyHK5vCJ0OYoMjTiO35xe8DcAeD70Bd2FBuE5ktcDOCuKokNDl6/IoNnBzE4g+VmSD4S+YPvQINwPYNbMyqVSaYfQhS/Sd61Wa5n3/p3pRf906IsyYGPwJMn1zWbz+FartSz0eRHpqTiO15mZA/Bo6Itv0H5m9jCAi+v1+oGhz5NI18zPzy83s5PM7NskXwl9oQ36D8AWM7vOe/8n8/Pzy0OfP5GOOOd2NbNzSD7UjwuH5GaSD5D8LoDLSV4E4DQzO8XMyt77t8VxvK7RaOydJMma8fHxnefn55fPz88vHx8f39k5t7bRaOwdx/E67/3bzKyczik4DcDFAC43sxvT/2NznxqDBwGc5ZzbNfT5FMllamrqAO99ZGYbenixP2BmXyf5aTN7XxRFh/bzblkul1dEUXSomb3PzBzJf+zlACaAZwDUqtXqfv06RpG2RFG0r/eeJF/qwZ39NgBN7/3J1Wp1n9DHuj3VanUf7/3JJGOSPwCwpcsNwYsAvCYcycBwzu2VTpXt2jt7AI+STLz3Jzvndg99jJ1KkmRNOn34CyQf72Kj+ByAmqYkSzBjY2M7Aji/W9NwAfyU5FSj0Xj7KL4Sa7Vay6IoOhxAleT93SgzM9sA4LwkSVaGPj4pkLSbu+RnXgBPmVk9juMjRvGi355Wq7WM5JEkG92YB0HyPu/9iaGPS0ZcvV5/i5nduMTK+grJb5nZSbpzlUpJkqw0s/el04W3LrFsv+O9f1PoY5IRkyTJSgAXLmWAz8w2mFl9ZmbmoNDHM6iazeZvpb2CjUvoVb1I8hNahCRd0Ww2jyb5H0u48B82s3MmJyd3C30sw8I5t3u6xLjjGZMkf0TyyNDHIkOqXC6vAFDt9FUWyYdI/qXuRJ1Le15/TfKRDnsDWwD8rWYUSlu8928ieXuHF/5jAE6rVCqrQh/HqEjfuJzR6X4IAG6t1WpvCH0cMgTM7K9IPtdBJfsVgPOdczuFPoZRNT4+vjOACzo5PyQ3Afiz0McgA8o5txPJKzq48LeS/GIURfuGPoaiqNfr+wO4ssPewOVjY2M7hj4GGSC1Wu1gknd2cFe503t/VOj8RdVoNN7RyQAtydvjOF4XOr8MAO/9e0j+ss27yIsAPq4BvvDSeQSfbPcVLcmfm9kJofNLQGZ2eruj/CRv0mSTwRPH8ZvN7PttnsvNZvbh0Nmlz9KpqI027/ovA/h4SfvYDaz5+fnlJD/R7n4FJKeLNBW70NLBvq+1efHfE8fxEaGzSz7pVur3tdkIXK3BwRHnnNuV5E1tVowrkiTZJXR2ac/ExMRqkn/X5rn+7sTExOrQ2aUHnHNrSf6gnS4/yY+qazi8Wq3WMjMba+eRgOQtSZKsCZ1dumh6evr1JH/UxsX/RBzHx4TOLd3RbDaPB/BUG+f/DufcXqFzSxekF/9P2jn59Xp9/9C5pbvq9fqB7cwZAPBfagSG3Nzc3B7t3PnN7DrtQDu6nHO7k7y+nZuBHgeGVDrg184z/6Wa2DP6yuXyCjO7rJ0xAQ0MDpn0VV/u0X4zcxrsK45Wq7UMQLWN+nGjXhEOiXSST+73/OnkHikgM/tkGz2Bq3WTGALtzPAzszND55WwzGysjUZgOnReWYSZnd7Gnf+M0HllMAA4q42bhtYODCLv/XvyLuxRt1+2lfdxIF1ApFWEgyRdz59rSS+Avw2dVwZT3oFBkv+t/QQGxPj4+M55N/MAcKkGcmR70gHkJGcjcLveDAyAvNt4Afim3vNLlnK5vCLvZCEAnw+dt9DM7K9ynqg7NMNP8pqcnNzNzO7KOSj4odB5CymKokPz7A4L4AnN7Zd21ev1A/MsICK5sdFoHBI6b6Gk3bTMffsBvKxVfdKpZrN5fJ6lxCRv0cdH+ijvaK2ZnR46qwy3vHMEzMyFzloIzWbz6Dzv+0leoRF/Wap03cDf5+htbjGz3w+dd6SlW0D/OMfJuEfbeEm3OOd2BfDTHDedO/WmqYcAXJjnuV8tsXRb+pXoPOMBk6GzjqR6vf6WPB9/0DRf6RUAF+RoAF6IouiNobOOHDO7MUfh31TSvv3SI+l3B27OcRO6IXTWkeK9PzlHob+oL/ZIr6U90V/neCvw3tBZR8LY2NiOJB/Qs5cMCjP7VI4G4N4kSVaGzjr0AJyv0VcZJJVKZVWet1Fmdm7orEPNObcXyU0ZXf+t+kS39JuZHZujAdgwNze3R+isQ8t7H+V49v9S6JxSTACuylE/q6FzDqUoivYF8HxG4f4qiqJ9Q2eVYpqamjogTx3VB0Y64L1njtb1/NA5pdjyTE4j2Qidc6hMTU0dkDXph+RjzrmdQmeVYkt3pHoyo66+UK1W9wmddWjkfPY/LXROkVKpVALwMY0FdEm6G8uGjBb1oUqlsip0VpFS6TdzVQA8mlFnf6kFajmY2Tk5nqn+MnROkYVIfiRHL+BjoXMOtHSu9UMZ71Yf1qQfGTSVSmUVyccyblwPlLRWZfvM7KQckyvOCZ1T5LWY2URW/fXenxg658Ays29nXPwbJicndwudU+S1JEmyJmvmKslrQ+ccSDMzMweRfCWjAaiHzimyGADNjHGALVNTUweEzjlwzMxltJyvzMzMHBQ6p8hiarXawQC2ZjQCF4bOOVDSjRezXqN8K3ROkTxIfifrNbY2rF3Ae//OHIN/J4XOKZKHmZ2Soz4fGzrnwCD52Ywu01PaXEGGRfpK8L8zGgCEzjkQ0nf/T2vwT0YJyTjjMeBxPQaUSiUzOyGruxTH8RGhc4q0o9lsHp1jTsA7Q+cMLqv7T/I+tZQybNKB7Qf1GJAhq5BIToXOKNIJADMZDcC9oTMGFcfxm7O6SY1G4+2hcy5FHMdHmNllJO8j+WuSvzazewF8Lo7jw0LnC22Uy4fkkTnqd3E/LU7y7IzR/0eHtfs/Pj6+M4DLM45vq5ldVsSNTYpQPq1Wa1nWZiEAzgidMxgAN2QUzudDZ+zE+Pj4zmb2/azWf8Fjzk3DWsk7UaTyAfCljDr+zdAZgyiXyyuyNlT03p8cOmcnsu5s26kInwudu1+KVD6zs7OVjMZt0/z8/PLQOfvOe39URsFsds7tHjpnu+I4PqLdyv1qd3fYn3nzKFr5zM3N7ZG1yC2KosND5+y7rOd/kreFztgJM7uskwqeVvJLQ+fvtSKWD8nbFzsuMzszdMa+AzCfcbKboTN2guR9S6jg94TO32tFLB8AlnGzuzp0xr4D8MRihTKsz/95vhy7SEV4KXT+Xiti+WQtDgLwaOiMfRVF0b5ZJ7vRaOwdOmcniljB21HE8qlWq/tlHVuhvh4E4N0ZJ/qB0Bk7ZWb3LqGC3x06f68VtXxybHZ7QuiMfQPgvIzC+HrojJ0C8LlOKziA2dD5e62o5UPynzIat7NDZ+wbAFdmFManQ2fsVBzHh2VtCbWdyr3Ve/+20Pl7rajl472/JKPOfzF0xr4heWdGD+B9oTMuRSevukh+NnTufili+eSYEHR76Ix9k7V1chRFh4bOuBTOuZ1I3tRG5f7e2NjYjqFz90sRy6fZbL41o4fzTOiMfTE9Pf26jJO9eRSmRjrndjKzyxbr7gLYSvKzw165O1G08kmSZGXWjMAkSdaEztlzWUskh/kNwGtJn3kvBXAPyZfS390AZof5mbZbilQ+ZvZwRs939KcEZ02KIPnd0BlFeiHrsWdYJ7+1JesVIIDLQ2cU6QUz+3JG3R8PnbHnSDYyegAXhc4o0gtZX78CMBM6Y8/laAVPC51RpBcAnFH43i/JaxcrBDM7JXRGkV4AcGpGA/CN0Bl7DsCtGQ1AOXRGkV6I4/hdGY+/N4fO2HNZi0FG7dWPyKuiKDo8owEY2sVOuZF8fLFCiON4XeiMIr1Qq9UOzngEGP19AUj+fLFCGNZ9AESyZO0LQPLp0Bl7juTGxQqhENMhpZCcc3tmNADPhs7YcwBeXKwQxsfHdw6dUaQXJiYmVmc8AjwfOmPPZS2IGIWFQCKvpVwur8hoALaEzthzagCkqNQAlPQIIMWlR4BS9iCgc25t6IwivaBBwJJeA0px6TVgSROBpLiyJgKRfCR0xp7TVGApKk0FLmkxkBSXFgOVtBxYikvLgUvaEESKSxuClLK3BANwceiMIr2gLcFK2hRUiovkFRl1f/Q3BQXw/owxgBtDZxTphaxtwc3spNAZe65oHwYReRXJRzLmwBwWOmPP5ZgOORKfBhNZSJ8GWyBrPcCwfxxUZFv6OOgCAO7IeBYa6s+Di2zLe/+BjDr/b6Ez9k3WaKiZudAZRbrJe3+J3n6lzOzcjHGAfwydUaSbAFyTUefPDp2xbwC8W28CpEiyPg0ex/EfhM7YN9VqdZ/FCmP9+vWtarW6T+icIt1Qr9f3z6rvzrm9Qufsq6x9AQrxrXQphNnZ2UpGj3f09wHYFsmvZhRKHDqjSDeYGTIGvb8SOmPfATgrowH4QeiMIt1A8t8zGoAzQ2fsO+/9URmvRbYUZmaUjCzn3J5ZMwALMQV4W+VyeQXJ5zJaRk0IkqGWtQkIyY2lUmmH0DmDIHl9RuF8IXRGkaXImvRG8trQGYPJMQ7weKvVWhY6p0iHdiD5dEYv9/TQIYOJoujQrPejURQdHjqnSCeyxrnWr1/fqtVqB4fOGRTJ+zMGA6uhMw4T59zaZrN5vJn9hZl9iuQcgGvM7Ick7yP5GIBfAHie5CskN5PcZGY/M7OHAdxD8mYA/0AyNrNzvfcfaDQabx8bG9sx9PENE+99lFG37wmdMTgAsxmPAffrMeC1OefWmtl7AdTM7NskH8u64yzllzYWPwHwDwDON7NjK5XKqtDlMIhardayrOm/AHzonMGZWTlHxTsydM5BUKlUVnnv32NmIPkjAFt7ecHnbBReIPk9ABfHcXyEGuvfaDQa78gqOzM7LnTOQbADySczKlkjdMhQkiTZBcCpaZd80Y1UBuFH8hEAs+nilmK+3iqVSgAso5weU2OZyvEY8HSSJCtD5+yXtPt4HMkvkNwU+qJeSmNAcqpoOzyNjY3tmPURXHX/F2g2m8fn6C6N/KSgJEl2IfkRkneHvnh70Bh8D8AfF2G/x6zFP+vXr2/FcXxM6JwDI8+ACcnrQ+fslenp6deRnCL5y9AXah8aggcAfMw5t1Pocu8VAP+8WBkAeDB0xoED4OKMQtvabDZ/K3TObnLO7QmgCuBXoS/MAA3BkyTPHh8f3zn0eeimRqNxSNaxm9mnQuccOPV6/UAAWzIqzUgMBo6Nje0I4Dwz29CDC2sjyVsAXEly2szONLOTvPf/q16vv2VmZuag6enp1zvndi2XyysqlcqqJEnWRFG0b6PROKTZbP5Os9n832b2QTObAGAkv2ZmPwbwcg/yPg7gz0ojMmAIwGfcyLbU6/X9Q+ccSGZ2XVblds7tHjrnUnjvT8ya/NTmxf4tABeY2XtnZmYO6uXIcqVSWdVoNH7Pe/+n3vuI5G0kN3fjWADcYWbH9ip7Pzjn1mb15gBcEzrnwPLe/0mO7tO5oXN2Ympq6gAz+/oSL/jNZvYvAMbjOD5iEAbUJiYmVpvZCd77S8zsri40aolzbs/Qx9UJkpM5Gro/Cp1zYM3Pzy8n+VBGAT46TK8E0wHOD3Xa3QfwIoB5M/ugc25t6OPJUqvVDgZwVtZ38DKO+alhu1AqlcoqAE9kNG73l0bkUadnSJ6do4L8deiceTjn1pL8Wod3wlsA/M0wb4pSq9UOJnlRp488AC6fmJhYHfo48iD50Ry91+Lt/NMu59yuJJ/NKMiHB30OehzHRwB4sIO7/ee9978bOn83tVqtZXEcv4vkte1OXwZwz6CXh3Nup6xNbgH8IkmSXUJnHQoAZnK0pgO7jtp7/6cAXmyjkj9D8qIibA3tvX8TgEtJ/rqN3tBzgzwRLGtfi7S+fiZ0zqFRrVb3y7qAADwxiO+QSX6ijYr9LIALh/3NRiempqYOILme5EttNJQfD517W+nszUU3/SD5XKPR2Dt01qGS9T41rRAXhM65rZwV+WWS8dzc3B6h84YWx/E6AFflbQRC590WyU/nON8zoXMOnUajsXfWpqEknxu0SRU5uoLXFW1xTB7e+6NI/mCYGoA4jteRfCGjjm6anp5+XeisQynPWACAK0PnXGiRivAkgPdrCeiidgBw2mKDwKEDLmRmX9Gzfw855/bM8/680Wi8I3TWV22nkbpK3f386vX6/iS/NcgNgJkdl+Pm9MwwzN0YaADOy9HK3jUok4O2uetv9N5/IHSmYdRqtZYB+Jttu9ihc5VKv5n0Q/InORqAs0JnHXpJkqwkeV9WYZP8ROispdL/awDM7K4oit4YOs+wazabv7Nwf4TQeUql7JWraX28u1wurwiddSR470/MUeAvDcLgWprlq5r00T2Tk5O7AfjGIDQA9Xr9t/PMYQDwh6GzjhSS38nRCPxr6AUyZlYvab53L+wAoBkyQPo5u1tyPJJeFzLnSEpnkGXOrhuURwEZPSQvynHnf77RaBwSOutIyjPLjuRm7/1RobPKaInj+JisDWvSBuC80FlHVtoF+1GORuC+YVlFJoPPObc7yQdydP1/GPoRdOSRPDJPS0zy7zTpRpaq1WotI/nVPD3POI4PC523EMzsM1knJG2Rx0JnleFmZufmqWsALgydtTDSR4Hb8rTKzWbz+NB5ZTiZWTlPb9PMvl/Sm5/+qtVqb8jz1RwAT9Xr9QND55XhMjMzc5CZ/SzHxb9h1LarHxre+z/P+Shw1+Tk5G6h88pwSJJkDYD/ylm3Phg6b6EBuDzPiSJ5vaZmSpYkSVaa2b/krFNzofMWXvohxttzttaX6c2AbE864v/FnBf/bYO+L2VhzMzMHJT1NdYFJ24qdF4ZPOn27fWcdejpqampA0JnlgXM7IS8X6kxs0+GziuDBcCFOV/3vey9/z+h88prMLMP5zmJaSOgOQJSKpVKJTM7J2+98d7/eei8sgiS02oEJK92Ln5t7zUE0oGcq9s4qXocKKB0x6Fc3f60639V6MySU/pm4LttnNyq3g4URzsDfumg33c04j9kJiYmVufZvGHBSU40T2D0pVvM5XrVl/YQv6/dnYZUOqPrjjYages1Y3B0JUmyJu8kn/Ti/2ERv9o0Upxze+Wd1pme9Lu0dmD0pHNF/rONevBjfdBjRKSNQO6eAICntIpwdJhZOc/CnoV3fl38IyZJkjVtjglsBnCWBgeHVzrSf16eJb0Ln/nV7R9RExMTq83sxryVIe0N/L1zbtfQ2aU9k5OTu+XZyWfb0X4N+I249BVh7nkCaSPw02azeXTo7JJPHMfH5NnDb5tzfJVe9RVE2jWstXl32AzgAm36OLjK5fIKABe30+VPu/2a4VdEZvbhvAuIFjQEN9fr9beEzi7/U/rFntxjPOld/2Uz+4vQ2SWgdBVhrqXECxqBl8zsU+oyhlepVFYBuDjP57q2uev/TKv6pFQqlUpxHK/Lu6nINpXox3EcHxM6f1GZ2XHtzPFY0IDfpvX88j+kg4NfaLcyvTqApArVP3EcrzOzr3RyrkjOqecm22VmH8qz2/BrNALPA7hwfHx859DHMKqSJNmF5KdJvtBBb22DNvCUXGq12hsA3NrhHeZJMztzbGxsx9DHMSqcczuRPJvk0x2ek5u1dbe0ZX5+frmZuXZfKS3oETxK8iPqbnauUqmsIvlRko93eOFvBnChXt1Kx8zs90ne2UkFTCvhY2Y2kSTJmtDHMiycc2sBnA/giU7L3cx+qG/1SVeknyKb7OTZc0FDsAlAs1arHRz6eAZVo9E4BIAH8KtOyxnA82Z2ru760nVRFL0RwA2dVs60gm4FcIOZnaLHg99082dnZysA/hnA1qWUrZld12g0Dgl9TDLizOy9ZnbvUiprWmF/RjJuNptHF2nVYavVWtZoNN4BwNqdhLWd3tXdAP4w9HFJgaSfkDrXzDYstQKnPYMHAcyQPHJEG4MdvPdHee8jM3u4S2X2DICztJWbBDM3N7cHgOpSnltf4472JIAvzc7OVubm5vYIfYydcs7tCeBUkld0+gpvO+Wzycw+45xbG/oYRUqlUqk0PT39epKNpQwUbucut4Xk7QDMzE6pVqv7hT7W7anX6/vPzs5WzAwk/53kK10ui+cBzGi3HhlY1Wp1HwBVkr/sZuXf5g74EMl/8t5fMjs7W2k2m29NkmRlv44xSZKVzWbzrd77D3jvLwFwTbe69du58H9hZp9pNBp79+sYRZYk3X1orN2NKZbQKLxiZg+TvMnMvpxOYjoDwKlxHL8riqLDa7XawdVqdT/n3J4TExOry+XyinK5vGJiYmK1c27ParW6X61WOziKosPjOH4XgFMBnGFmLu3G30TykW7f2Rc5pvvN7Ezt0iNDa35+frn3/kSS13Y6q7BIPwBbAFwD4I9KpdIOoc+fSNdMTU0dAOBCkg+FvtAG7Ze+BbmgXq/vH/o8ifRU+mmqY80MS5nuOuw/ko8B8NpTQQqr1Wot896/08zQjclFg/4DcA8Ab2bHjeh8B5HOpUuRzwDwzU72JRi0H8mNJK81s9O1DkKkDfPz88ujKDrczM4keTWAR0Nf0Dku+EfM7Ctmdma6Gk8DeSLd4pzbK93I9GySX0wnCT0ToCv/jJn9G4DLSZ4dx/EfOOf2Cl0+IoWUJMmaKIoO996fDGAcwAyAywF8g+TN6YKZR0k+TfLZdFbdlvT3PMln0797hOTdJG8G8I3035gBMG5mJ8VxfJj2NhAREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREfn//V8Ftpkh0er+GAAAAABJRU5ErkJggg==\" height=\"32\" width=\"32\">" +
            "      </a>" +
            "    </div>" +
            "</body></html>"
    }
}
