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

package org.alephium.explorer.persistence.queries.result

import slick.jdbc.{GetResult, PositionedResult}

import org.alephium.explorer.api.model.Transaction
import org.alephium.explorer.persistence.schema.CustomGetResult._
import org.alephium.util.U256

object GasFromTxsQR {

  implicit val gasFromTxsQRGetResult: GetResult[GasFromTxsQR] =
    (result: PositionedResult) =>
      GasFromTxsQR(
        txHash    = result.<<,
        gasAmount = result.<<,
        gasPrice  = result.<<
    )

}

/** Query result for [[org.alephium.explorer.persistence.queries.TransactionQueries.gasFromTxsSQL]] */
final case class GasFromTxsQR(txHash: Transaction.Hash, gasAmount: Int, gasPrice: U256) {

  def gasInfo(): (Int, U256) =
    (gasAmount, gasPrice)

}
