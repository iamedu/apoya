(ns apoya.monitor.files
  (:import [fortress.ring.http MultipartProgressListener]))

(defn build-upload-listener []
  (proxy [MultipartProgressListener] []
    (uploadStarted [request])
    (bytesWritten [byte-count])
    (uploadFinished [])))
