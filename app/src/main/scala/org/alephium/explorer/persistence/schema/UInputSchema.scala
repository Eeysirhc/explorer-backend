// Copyright 2018 The Alephium Authors
// This file is part of the alephium project.
//
// The library is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// The library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with the library. If not, see <http://www.gnu.org/licenses/>.

package org.alephium.explorer.persistence.schema

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.{Index, PrimaryKey, ProvenShape}

import org.alephium.explorer.Hash
import org.alephium.explorer.api.model.Transaction
import org.alephium.explorer.persistence.model.UInputEntity

trait UInputSchema extends CustomTypes {
  val config: DatabaseConfig[JdbcProfile]

  import config.profile.api._

  class UInputs(tag: Tag) extends Table[UInputEntity](tag, "uinputs") {
    def txHash: Rep[Transaction.Hash]     = column[Transaction.Hash]("tx_hash")
    def scriptHint: Rep[Int]              = column[Int]("script_hint")
    def outputRefKey: Rep[Hash]           = column[Hash]("output_ref_key")
    def unlockScript: Rep[Option[String]] = column[Option[String]]("unlock_script")

    def pk: PrimaryKey = primaryKey("uinputs_pk", (outputRefKey, txHash))

    def uinputsTxHashIdx: Index = index("uinputs_tx_hash_idx", txHash)

    def * : ProvenShape[UInputEntity] =
      (txHash, scriptHint, outputRefKey, unlockScript)
        .<>((UInputEntity.apply _).tupled, UInputEntity.unapply)
  }

  val uinputsTable: TableQuery[UInputs] = TableQuery[UInputs]
}