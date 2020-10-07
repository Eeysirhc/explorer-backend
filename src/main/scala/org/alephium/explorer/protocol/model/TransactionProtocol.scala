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

package org.alephium.explorer.protocol.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import org.alephium.explorer.api.model.{BlockEntry, Transaction}
import org.alephium.explorer.persistence.model.TransactionEntity
import org.alephium.rpc.CirceUtils.avectorCodec
import org.alephium.util.{AVector, TimeStamp}

final case class TransactionProtocol(
    hash: Transaction.Hash,
    inputs: AVector[InputProtocol],
    outputs: AVector[OutputProtocol]
) {
  def toEntity(blockHash: BlockEntry.Hash, timestamp: TimeStamp): TransactionEntity =
    TransactionEntity(
      hash,
      blockHash,
      timestamp
    )
}

object TransactionProtocol {
  implicit val codec: Codec[TransactionProtocol] = deriveCodec[TransactionProtocol]
}
