import Foundation
import Photos
import UniformTypeIdentifiers
import UIKit.UIImage
import UIKit.UIScreen
import ComposeApp

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
    
    func getURLCancellableRequest(completion: @escaping (IosMediaElement?) -> Void) -> Request? {
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
                    var mediaElement = contentEditingInput?.toMediaElement()
                    completion(mediaElement)
                }
            )
        )

    return request
    }
}

extension PHContentEditingInput {
    func toMediaElement() -> IosMediaElement? {
        return if (mediaType == .image){
            self.toImage()
        } else {
            (self.avAsset as? AVURLAsset)?.toVideoElement()
        }
    }
    
    func toImage() -> IosMediaElementImage {
        var path = self.fullSizeImageURL?.path
        var dateNative = self.creationDate?.timeIntervalSince1970
        
        var date: KotlinDouble? = nil
        
        if (dateNative != nil) {
            date = KotlinDouble(value: dateNative!)
        }
        
        return IosMediaElementImage(path: path, date: date)
    }
}

extension AVURLAsset {
    func toVideoElement() -> IosMediaElementVideo {
        var path = self.url.path
        var dateNative = self.creationDate?.dateValue?.timeIntervalSince1970
        var duration = CMTimeGetSeconds(self.duration)
        
        var date: KotlinDouble?
        if (dateNative != nil) {
            date = KotlinDouble(value: dateNative!)
        }
        
        return IosMediaElementVideo(path: path, date: date, duration: duration)
    }
}

extension PHAsset {
    func getIosMediaElement() async -> IosMediaElement? {
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
