import {CircularProgress} from "@mui/material";
import NoLogs from "../general/NoLogs.jsx";
import ExternalTimeLog from "./ExternalTimeLog.jsx";
import useExternalTimeLogMutations from "../../hooks/useExternalTimeLogMutations.js";

export default function ExternalTimeLogList({externalTimeLogs, isExternalTimeLogListing, isExternalServiceEditMode}) {
  const {onDelete} = useExternalTimeLogMutations();

  if (isExternalTimeLogListing) {
    return <div className="text-center">
      <CircularProgress />
    </div>
  }

  return (
    <div className={`m-4`}>
      <div className="flex flex-col items-center">
        <div className="w-full overflow-x-auto">
          {externalTimeLogs.length
            ? externalTimeLogs.map(externalTimeLog =>
              <ExternalTimeLog
                key={externalTimeLog.id}
                externalTimeLog={externalTimeLog}
                onDelete={onDelete}
                isExternalServiceEditMode={isExternalServiceEditMode}
              />)
            : <NoLogs />
          }
        </div>
      </div>
    </div>
  );
}