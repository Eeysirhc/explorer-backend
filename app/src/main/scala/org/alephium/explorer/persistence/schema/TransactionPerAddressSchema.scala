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

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Index, PrimaryKey, ProvenShape}

import org.alephium.explorer.api.model.{Address, BlockEntry, Transaction}
import org.alephium.explorer.persistence.model.TransactionPerAddressEntity
import org.alephium.explorer.persistence.schema.CustomJdbcTypes._
import org.alephium.util.TimeStamp

object TransactionPerAddressSchema
    extends SchemaMainChain[TransactionPerAddressEntity]("transaction_per_addresses") {

  class TransactionPerAddresses(tag: Tag) extends Table[TransactionPerAddressEntity](tag, name) {
    def address: Rep[Address]           = column[Address]("address")
    def txHash: Rep[Transaction.Hash]   = column[Transaction.Hash]("tx_hash", O.SqlType("BYTEA"))
    def blockHash: Rep[BlockEntry.Hash] = column[BlockEntry.Hash]("block_hash", O.SqlType("BYTEA"))
    def timestamp: Rep[TimeStamp]       = column[TimeStamp]("block_timestamp")
    def txOrder: Rep[Int]               = column[Int]("tx_order")
    def mainChain: Rep[Boolean]         = column[Boolean]("main_chain")

    def pk: PrimaryKey = primaryKey("txs_per_address_pk", (txHash, blockHash, address))

    def hashIdx: Index      = index("txs_per_address_tx_hash_idx", txHash)
    def timestampIdx: Index = index("txs_per_address_timestamp_idx", timestamp)
    def blockHashIdx: Index = index("txs_per_address_block_hash_idx", blockHash)
    def addressIdx: Index   = index("txs_per_address_address_idx", address)

    def * : ProvenShape[TransactionPerAddressEntity] =
      (address, txHash, blockHash, timestamp, txOrder, mainChain)
        .<>((TransactionPerAddressEntity.apply _).tupled, TransactionPerAddressEntity.unapply)
  }

  val table: TableQuery[TransactionPerAddresses] = TableQuery[TransactionPerAddresses]
}
