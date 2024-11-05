import {Tooltip} from "@mui/material";
import WarningAmberIcon from "@mui/icons-material/WarningAmber.js";
import TimeLogSyncStatusIcon from "./TimeLogSyncStatusIcon.jsx";
import RunningWithErrorsIcon from "@mui/icons-material/RunningWithErrors.js";
import {isExternalServiceSyncingEnabled, isJiraSyncingEnabled} from "../../config/config.js";

export default function TimeLogStatusIcons({
  isConflicted,
  isContinueUntilTomorrow,
  jiraSyncStatus,
  externalTimeLogSyncStatus,
  showOnlyNotSuccessfullySynced
}) {
  return (
    <div className="flex">
      {isConflicted && <Tooltip className="ml-1" title="Conflicted"><WarningAmberIcon color="warning" className="text-red" /></Tooltip>}
      {isContinueUntilTomorrow && <Tooltip className="ml-1" title="Timelog continues tomorrow"><RunningWithErrorsIcon color="warning"
                                                                                                                      className="text-red" /></Tooltip>}
      {isJiraSyncingEnabled && <TimeLogSyncStatusIcon serviceName="Jira" className="ml-1" status={jiraSyncStatus}
                                                      showOnlyNotSuccessfullySynced={showOnlyNotSuccessfullySynced} />}
      {isExternalServiceSyncingEnabled &&
        <TimeLogSyncStatusIcon serviceName="External Service" className="ml-1" status={externalTimeLogSyncStatus}
                               showOnlyNotSuccessfullySynced={showOnlyNotSuccessfullySynced} />}
    </div>
  )
}