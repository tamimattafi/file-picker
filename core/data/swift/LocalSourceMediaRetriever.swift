import Foundation
import Photos
import UniformTypeIdentifiers
import UIKit.UIImage
import UIKit.UIScreen
// Change to your shared framework, for example ComposeApp
import YOUR_SHARED_FRAMEWORK

/*
    Most of the reference are taken from https://github.com/exyte/MediaPicker
*/

class LocalSourceMediaRetriever : ILocalSourceMediaRetriever {

    func handleInput(phAsset: MediaAsset, handler: IMediaElementHandler) {
        Task {
            let element = await phAsset.asset.getIosMediaElement()
            handler.handleElement(element: element)
        }
    }
}

extension PHAsset {
    actor RequestStore {
        var request: Request?

        func storeRequest(_ request: Request) {
            self.request = request
        }

        func cancel(asset: PHAsset) {
            switch request {
            case .contentEditing(let id):
                asset.cancelContentEditingInputRequest(id)
            case .none:
                break
            }
        }
    }

    enum Request {
        case contentEditing(PHContentEditingInputRequestID)
    }

    func getURLCancellableRequest(completion: @escaping (PlatformMediaElement?) -> Void) -> Request? {
        var request: Request?

        let options = PHContentEditingInputRequestOptions()
        options.isNetworkAccessAllowed = true
        options.canHandleAdjustmentData = { _ -> Bool in
            return true
        }
        request = .contentEditing(
            requestContentEditingInput(
                with: options,
                completionHandler: { (contentEditingInput, _) in
                    let mediaElement = contentEditingInput?.toMediaElement()
                    completion(mediaElement)
                }
            )
        )

    return request
    }
}

extension PHContentEditingInput {
    func toMediaElement() -> PlatformMediaElement? {
        return if (mediaType == .image){
            self.toImage()
        } else {
            (self.avAsset as? AVURLAsset)?.toVideoElement()
        }
    }

    func toImage() -> PlatformMediaElementImage {
        let path = self.fullSizeImageURL?.path
        let dateNative = self.creationDate?.timeIntervalSince1970

        var date: KotlinDouble? = nil

        if (dateNative != nil) {
            date = KotlinDouble(value: dateNative!)
        }

        return PlatformMediaElementImage(path: path, date: date)
    }
}

extension AVURLAsset {
    func toVideoElement() -> PlatformMediaElementVideo {
        let path = self.url.path
        let dateNative = self.creationDate?.dateValue?.timeIntervalSince1970
        let duration = CMTimeGetSeconds(self.duration)

        var date: KotlinDouble?
        if (dateNative != nil) {
            date = KotlinDouble(value: dateNative!)
        }

        return PlatformMediaElementVideo(path: path, date: date, duration: duration)
    }
}

extension PHAsset {
    func getIosMediaElement() async -> PlatformMediaElement? {
        let requestStore = RequestStore()

        return await withTaskCancellationHandler {
            await withCheckedContinuation { continuation in
                let request = getURLCancellableRequest { url in
                    continuation.resume(returning: url)
                }
                if let request = request {
                    Task {
                        await requestStore.storeRequest(request)
                    }
                }
            }
        } onCancel: {
            Task {
                await requestStore.cancel(asset: self)
            }
        }
    }
}
