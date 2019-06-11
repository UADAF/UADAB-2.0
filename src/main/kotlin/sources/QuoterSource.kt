package sources

import quoter.Quoter

object QuoterSource : BasicExternalSource<Quoter>() {
    override suspend fun load() = with(UADAB.cfg) { Quoter(quoterUrl, quoterAccessKey, quoterDefaultRepo) }
}