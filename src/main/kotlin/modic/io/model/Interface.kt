/**
 * Copyright 2023 Karl Kegel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package modic.io.model

import jakarta.persistence.*
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlTransient
import java.util.*

/**
 * The [Interface] contains the compatability information of a [AssociationRelation] target in the variability
 * time-space.
 * An Interface can contain multiple types of intervals and points describing subsets of time-space.
 * An AssociationRelation regarding a certain target type is fulfilled if the target fulfils at least one of the
 * subsets defined by this class.
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Interface(

    /**
     * Technical database (JPA) identifier used for relation joins.
     * The [dataID] is system specific and not exported to XML.
     * It must not be used to identify elements in distributed use-cases.
     * It should not be used to identify elements from outside the service. All model elements provide other
     * suitable identifiers to be used.
     */
    @field:Id
    @field:Column
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:XmlTransient
    var dataID: Long? = null,

    /**
     * The list of version-based intervals open on the left side (past).
     * @see [LeftOpen]
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "LeftOpen")
    private val leftOpenDelimiters: MutableList<LeftOpen> = LinkedList(),

    /**
     * The list of version-based intervals open on the right side (future).
     * @see [RightOpen]
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "RightOpen")
    private val rightOpenDelimiters: MutableList<RightOpen> = LinkedList(),

    /**
     * The list of version-based intervals delimited on both sides (past and future).
     * @see [Region]
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Region")
    private val regionDelimiters: MutableList<Region> = LinkedList(),

    /**
     * The list of variant-based (and/or version-based) points / ranges
     * @see [Point]
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Point")
    private val pointDelimiters: MutableList<Point> = LinkedList(),

    /**
     * Backlink to [Model] to improve traversal.
     */
    @field:Transient
    @field:XmlTransient
    var associationRelation: AssociationRelation? = null
) {

    constructor() : this(null)

    fun initializeZeroIDs(){
        dataID = 0
        leftOpenDelimiters.forEach { e -> e.initializeZeroIDs() }
        rightOpenDelimiters.forEach { e -> e.initializeZeroIDs() }
        regionDelimiters.forEach { e -> e.initializeZeroIDs() }
        pointDelimiters.forEach { e -> e.initializeZeroIDs() }
    }

    fun getLeftOpenDelimiters(): List<LeftOpen> = leftOpenDelimiters

    fun addOLeftOpenDelimiter(leftOpen: LeftOpen) {
        if (!leftOpenDelimiters.contains(leftOpen)) leftOpenDelimiters.add(leftOpen)
    }

    fun removeLeftOpenDelimiter(leftOpen: LeftOpen) {
        leftOpenDelimiters.remove(leftOpen)
    }

    fun getRightOpenDelimiters(): List<RightOpen> = rightOpenDelimiters

    fun addRightOpenDelimiter(rightOpen: RightOpen) {
        if (!rightOpenDelimiters.contains(rightOpen)) rightOpenDelimiters.add(rightOpen)
    }

    fun removeRightOpenDelimiter(rightOpen: RightOpen) {
        rightOpenDelimiters.remove(rightOpen)
    }

    fun getIntervalDelimiters(): List<Region> = regionDelimiters

    fun addIntervalDelimiter(region: Region) {
        if (!regionDelimiters.contains(region)) regionDelimiters.add(region)
    }

    fun removeIntervalDelimiter(region: Region) {
        regionDelimiters.remove(region)
    }

    fun getPointDelimiters(): List<Point> = pointDelimiters

    fun addPointDelimiter(point: Point) {
        if (!pointDelimiters.contains(point)) pointDelimiters.add(point)
    }

    fun removePointDelimiter(point: Point) {
        pointDelimiters.remove(point)
    }

}
