import {Tooltip} from "@mui/material";
import WarningAmberIcon from "@mui/icons-material/WarningAmber.js";
import TimeLogJiraSyncStatusIcon from "./TimeLogJiraSyncStatusIcon.jsx";
import RunningWithErrorsIcon from "@mui/icons-material/RunningWithErrors.js";
import {isJiraSyncingEnabled, isUpworkSyncingEnabled} from "../../config/config.js";
import TimeLogUpworkSyncStatusIcon from "./TimeLogUpworkSyncStatusIcon.jsx";

export default function TimeLogStatusIcons({isConflicted, isContinueUntilTomorrow, jiraSyncStatus, upworkSyncStatus, showOnlyNotSuccessfullySynced}) {
  return (
    <div className="flex">
      {isConflicted && <Tooltip className="ml-1" title="Conflicted"><WarningAmberIcon color="warning" className="text-red" /></Tooltip>}
      {isContinueUntilTomorrow && <Tooltip className="ml-1" title="Timelog continues tomorrow"><RunningWithErrorsIcon color="warning" className="text-red" /></Tooltip>}
      {isJiraSyncingEnabled && <TimeLogJiraSyncStatusIcon className="ml-1" status={jiraSyncStatus} showOnlyNotSuccessfullySynced={showOnlyNotSuccessfullySynced} /> }
      {isUpworkSyncingEnabled && <TimeLogUpworkSyncStatusIcon className="ml-1" status={upworkSyncStatus} showOnlyNotSuccessfullySynced={showOnlyNotSuccessfullySynced} /> }
    </div>
  )
}