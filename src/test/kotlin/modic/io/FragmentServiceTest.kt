package modic.io

import modic.io.model.Delta
import modic.io.model.Fragment
import modic.io.model.Trace
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.util.*

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FragmentServiceTest {

    @Autowired
    lateinit var fragmentRepository: FragmentRepository

    @Test
    fun repositoryInsertFragmentTest(){
        val d1 = Delta(null, "foo", "t1")
        val d2 = Delta(null, "bar", "t2")
        val trace = Trace(null, LinkedList<Delta>())
        trace.addDelta(d1)
        trace.addDelta(d2)
        val fragment = Fragment(null, false, null, Optional.empty(), trace)
        val fragment2 = fragmentRepository.save(fragment)
        assertEquals(fragment2.dataID, 1)
        assertEquals(fragment2.trace?.getDeltas()?.size, 2)
    }

    @Test
    fun repositoryInsertFragmentTest2(){
        val d1 = Delta(null, "foo", "t1")
        val d2 = Delta(null, "bar", "t2")
        val trace = Trace(null, LinkedList<Delta>())
        trace.addDelta(d1)
        trace.addDelta(d2)
        val fragment = Fragment(null, false, null, Optional.empty(), trace)
        val fragment2 = fragmentRepository.save(fragment)
        assertEquals(fragment2.dataID, 1)
        assertEquals(fragment2.trace?.getDeltas()?.size, 2)
    }

}