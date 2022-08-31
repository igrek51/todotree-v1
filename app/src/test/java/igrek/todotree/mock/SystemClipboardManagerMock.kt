package igrek.todotree.mock

import igrek.todotree.service.clipboard.SystemClipboardManager

class SystemClipboardManagerMock : SystemClipboardManager() {

    override fun copyToSystemClipboard(text: String?) {}

}