import SyncProgressInfo from './SyncProgressInfo.jsx';
import dateTimeService from "../../service/dateTimeService.js";
import dayjs from "dayjs";
import useJiraSync from "../../hooks/useJiraSync.js";

export default function SyncAllProgressInfo() {
  const {
    isSyncingLaunched,
    isSyncingRunning,
    progressInfo: {
      totalTimeSpent,
      totalEstimate,
      inProgress,
      lastSyncedAt,
      progress,
      currentIssueNumber,
      totalIssues,
      duration
    }
  } = useJiraSync();

  return (
    <>
      {!isSyncingLaunched && !isSyncingRunning && (
        <SyncProgressInfo className="my-10 text-center text-blue-500 text-2xl font-bold">
          Start synchronizing to show detailed logs!
        </SyncProgressInfo>
      )}

      {lastSyncedAt && (
        <SyncProgressInfo className="mt-6">
          {`Last successfully synced at ${dateTimeService.getFormattedDateTime(dayjs(lastSyncedAt))}`}
        </SyncProgressInfo>
      )}

      {!inProgress && totalTimeSpent !== "0d 0h" && totalEstimate && (
        <SyncProgressInfo className="mt-6">
          {`Total time spent: ${totalTimeSpent}. Total estimate: ${totalEstimate}`}
        </SyncProgressInfo>
      )}

      {progress > 0 && (
        <SyncProgressInfo className="mt-6">
          {`Finished: ${currentIssueNumber}/${totalIssues} (${Math.round(progress)}%) issues in ${duration}`}
        </SyncProgressInfo>
      )}
    </>
  );
}
