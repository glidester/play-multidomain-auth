package controllers.common

import controllers.Assets.Asset
import controllers.AssetsBuilder
import play.api.http.DefaultHttpErrorHandler

abstract class MyAssetsTrait extends AssetsBuilder(DefaultHttpErrorHandler) {
	def public (path: String, file: Asset) = versioned(path, file)
	def lib (path: String, file: Asset) = versioned(path, file)
	def css (path: String, file: Asset) = versioned(path, file)
	def commonCss (path: String, file: Asset) = versioned(path, file)
	def js (path: String, file: Asset) = versioned(path, file)
	def commonJs (path: String, file: Asset) = versioned(path, file)
	def img (path: String, file: Asset) = versioned(path, file)
	def commonImg (path: String, file: Asset) = versioned(path, file)
	def rsc (path: String, file: Asset) = versioned(path, file)
}