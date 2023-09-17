package modic.io.model.xml

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.Instant


class XMLDateTimeAdaptor : XmlAdapter<String, Instant>() {

    override fun marshal(v: Instant): String {
        return v.toString()
    }

    override fun unmarshal(v: String): Instant {
        return Instant.parse(v)
    }

}