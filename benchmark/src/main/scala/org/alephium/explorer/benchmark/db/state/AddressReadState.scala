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

package org.alephium.explorer.benchmark.db.state

import java.math.BigInteger

import scala.concurrent.{Await, ExecutionContext}
import scala.util.Random

import akka.util.ByteString
import org.openjdk.jmh.annotations.{Scope, State}

import org.alephium.crypto.Blake2b
import org.alephium.explorer.{BlockHash, Hash}
import org.alephium.explorer.api.model._
import org.alephium.explorer.benchmark.db.{DBConnectionPool, DBExecutor}
import org.alephium.explorer.benchmark.db.BenchmarkSettings._
import org.alephium.explorer.benchmark.db.state.ListBlocksReadStateSettings._
import org.alephium.explorer.persistence.dao.{BlockDao,TransactionDao}
import org.alephium.explorer.persistence.model.{BlockEntity, BlockHeader, TransactionEntity}
import org.alephium.explorer.persistence.queries.TransactionQueries
import org.alephium.explorer.persistence.schema._
import org.alephium.util.{Base58, TimeStamp, U256}

/**
  * JMH state for benchmarking reads from TransactionDao
  */
class AddressReadState(val db: DBExecutor)
    extends ReadBenchmarkState[BlockEntity](testDataCount = 100, db = db)
    with TransactionQueries
    with BlockHeaderSchema
    with InputSchema
    with OutputSchema
    {

  implicit val executionContext: ExecutionContext = ExecutionContext.global
  import config.profile.api._

  val blockDao: BlockDao =
    BlockDao(4, config)(db.config.db.ioExecutionContext)

  val dao: TransactionDao =
    TransactionDao(config)(db.config.db.ioExecutionContext)

  val address:Address = Address.unsafe(Base58.encode(Hash.generate.bytes))

    val pagination:Pagination =Pagination.unsafe(
      offset  = 0,
      limit   = 1000000,
      reverse = false
    )

  private def generateTransaction(blockHash:BlockEntry.Hash, timestamp:TimeStamp): TransactionEntity =
      TransactionEntity(
        hash      = new Transaction.Hash(Hash.generate),
        blockHash = blockHash,
        timestamp = timestamp,
        chainFrom = GroupIndex.unsafe(1),
        chainTo   = GroupIndex.unsafe(3),
        gasAmount = 0,
        gasPrice  = U256.unsafe(0),
        index     = 0,
        mainChain = true
      )

  def generateData(currentCacheSize: Int): BlockEntity = {
      val blockHash = new BlockEntry.Hash(BlockHash.generate)
      val timestamp = TimeStamp.now()
    BlockEntity(
      hash         = blockHash,
      timestamp    = timestamp,
      chainFrom    = GroupIndex.unsafe(1),
      chainTo      = GroupIndex.unsafe(16),
      height       = Height.genesis,
      deps = Seq.empty,
      transactions = Seq(generateTransaction(blockHash, timestamp)),
      inputs = Seq.empty,
      outputs = Seq.empty,
      mainChain    = true,
      nonce        = ByteString.emptyByteString,
      version      = 0,
      depStateHash = Blake2b.generate,
      txsHash      = Blake2b.generate,
      target       = ByteString.emptyByteString,
      hashrate     = BigInteger.ONE
    )
  }

  def persist(cache: Array[BlockEntity]): Unit = {
    logger.info(s"Generating transactions data.")

    //drop existing tables
    val _ = db.dropTableIfExists(blockHeadersTable)
    val _ = db.dropTableIfExists(transactionsTable)
    val _ = db.dropTableIfExists(inputsTable)
    val _ = db.dropTableIfExists(outputsTable)


    val createTable =
      blockHeadersTable.schema.create
        .andThen(transactionsTable.schema.create)
        .andThen(inputsTable.schema.create)
        .andThen(outputsTable.schema.create)
        .andThen(createBlockHeadersIndexesSQL())
        .andThen(createTransactionMainChainIndex())
        .andThen(createInputMainChainIndex())
        .andThen(createOutputMainChainIndex())

    val _ = db.runNow(
      action  = createTable,
      timeout = batchWriteTimeout
    )

  Await.result(blockDao.insertAll(cache), requestTimeout)

    logger.info("Persisting data complete")
  }
}

// scalastyle:off magic.number

/**
  * JMH State For forward iteration with HikariCP.
  *
  *  Reverse benchmark with HikariCP is not required because
  *  these benchmarks are actually for when connection pooling is
  *  disabled to prove that raw SQL queries are faster with minimal
  *  connections whereas typed queries require more connections to be faster.
  */
@State(Scope.Thread)
@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
class Address_ReadState(override val db: DBExecutor)
    extends AddressReadState(db                   = db) {

  def this() = {
    this(db = DBExecutor(dbName, dbHost, dbPort, DBConnectionPool.HikariCP))
  }
}
// scalastyle:on magic.number

