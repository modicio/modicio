package modic.io.model.xml

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.sql.Timestamp


class XMLDateTimeAdaptor : XmlAdapter<String, Timestamp>() {

    override fun marshal(v: Timestamp?): String {
        return v.toString().replace(' ', 'T')
    }

    override fun unmarshal(v: String?): Timestamp {
        return Timestamp.valueOf(v?.replace('T', ' '))
    }

}