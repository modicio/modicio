package modic.io.repository

import modic.io.model.Fragment
import org.springframework.data.repository.CrudRepository

interface FragmentRepository : CrudRepository<Fragment, Long>