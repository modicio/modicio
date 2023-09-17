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

@Entity
class Interface(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var dataID: Long?,
    @OneToMany(cascade = [CascadeType.ALL])
    private val leftOpenDelimiters: MutableList<LeftOpen>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val rightOpenDelimiters: MutableList<RightOpen>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val regionDelimiters: MutableList<Region>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val pointDelimiters: MutableList<Point>,
    @Transient
    var associationRelation: AssociationRelation?
) {

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
