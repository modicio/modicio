package modic.io.model.xml

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.sql.Timestamp
import java.time.Instant


class XMLDateTimeAdaptor : XmlAdapter<String, Timestamp>() {

    override fun marshal(v: Instant): String {
        return v.toString()
    }

    override fun unmarshal(v: String): Timestamp {
        return Instant.parse(v)
    }

}