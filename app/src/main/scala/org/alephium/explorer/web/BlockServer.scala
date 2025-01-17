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

package org.alephium.explorer.web

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

import org.alephium.api.ApiError
import org.alephium.explorer.api.BlockEndpoints
import org.alephium.explorer.cache.BlockCache
import org.alephium.explorer.service.BlockService

class BlockServer(implicit val executionContext: ExecutionContext,
                  dc: DatabaseConfig[PostgresProfile],
                  blockCache: BlockCache)
    extends Server
    with BlockEndpoints {
  val route: Route =
    toRoute(getBlockByHash.serverLogic[Future] { hash =>
      BlockService
        .getLiteBlockByHash(hash)
        .map(_.toRight(ApiError.NotFound(hash.value.toHexString)))
    }) ~
      toRoute(getBlockTransactions.serverLogicSuccess[Future] {
        case (hash, pagination) => BlockService.getBlockTransactions(hash, pagination)
      }) ~
      toRoute(listBlocks.serverLogicSuccess[Future](BlockService.listBlocks(_)))
}
