import dateTimeService from "../service/dateTimeService.js";
import dayjs from "dayjs";
import {CircularProgress} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import ProgressInfo from "../components/ProgressInfo.jsx";

export default function SyncPage() {
  const {
    isSyncingLaunched,
    isSyncingRunning,
    progressInfo: {
      totalTimeSpent,
      totalEstimate,
      inProgress,
      lastSyncedAt,
      progress,
      worklogInfos,
      currentIssueNumber,
      totalIssues,
      duration
    }
  } = useAppContext();


  if (isSyncingLaunched && !isSyncingRunning) {
    return (
      <div className="absolute inset-1/2">
        <CircularProgress />
      </div>
    );
  }

  return (
    <div className="w-3/5 mx-auto">
      {!isSyncingLaunched && (
        <ProgressInfo className="my-10 text-center text-blue-500 text-2xl font-bold">Start synchronizing to show detailed logs!</ProgressInfo>
      )}
      {lastSyncedAt &&
        <ProgressInfo className="mt-6">{`Last successfully synced at ${dateTimeService.getFormattedDateTime(dayjs(lastSyncedAt))}`}</ProgressInfo>
      }
      {!inProgress && totalTimeSpent !== "0d 0h" && totalEstimate &&
        <ProgressInfo className="mt-6">{`Total time spent: ${totalTimeSpent}. Total estimate: ${totalEstimate}`}</ProgressInfo>
      }
      {progress > 0 &&
        <ProgressInfo className="mt-6">{`Finished: ${currentIssueNumber}/${totalIssues} (${Math.round(progress)}%) issues in ${duration}`}</ProgressInfo>
      }
      {isSyncingRunning && (<div className="mt-8">
        {worklogInfos?.map((worklogInfo, index) => (
          <ProgressInfo className="mt-2" key={`${Date.now()}-${index}`}>{worklogInfo.date} | {worklogInfo.ticket} | {worklogInfo.comment}</ProgressInfo>
        ))}
       </div>
      )}
    </div>
  )
}