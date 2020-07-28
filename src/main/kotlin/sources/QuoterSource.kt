package sources

import io.ktor.client.engine.apache.Apache
import quoter.Quoter

object QuoterSource : BasicExternalSource<Quoter<*>>() {
    override suspend fun load() = with(UADAB.cfg) { Quoter(quoterUrl, Apache, quoterAccessKey, quoterDefaultRepo) }
}