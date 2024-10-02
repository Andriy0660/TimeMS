import useWorklogSync from "../hooks/useWorklogSync.js";
import Label from "../components/Label.jsx";

export default function SyncPage() {
  const {progressInfo: {progress, worklogInfos, currentIssueNumber, totalIssues, duration}} = useWorklogSync();
  return (
    <div className="w-3/5 mx-auto">
      {
        progress > 0 ? (
          <div className="m-4">
            <div className="my-2 p-2 bg-gray-100 rounded-xl">
              {`Finished ${currentIssueNumber}/${totalIssues} (${Math.floor(progress)}%) in ${duration}`}
            </div>
            {worklogInfos.map((worklogInfo, index) => (
              <div key={`${Date.now()}-${index}`}
                   className="text-center p-2 w-full overflow-x-auto shadow-md bg-gray-50">{worklogInfo.ticket} {worklogInfo.comment}</div>
            ))}
          </div>
        ) : (
          <div className="flex justify-center mt-10 text-2xl">
            <Label label="Start synchronizing to show detailed logs!"/>
          </div>
        )
      }
    </div>
  )
}