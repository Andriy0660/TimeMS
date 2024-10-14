import {Tooltip} from "@mui/material";
import WarningAmberIcon from "@mui/icons-material/WarningAmber.js";
import TimeLogJiraSyncStatusIcon from "./TimeLogJiraSyncStatusIcon.jsx";
import RunningWithErrorsIcon from "@mui/icons-material/RunningWithErrors.js";
import {isJiraSyncingEnabled} from "../config/config.js";

export default function TimeLogStatusIcons({isConflicted, isContinueUntilTomorrow, jiraSyncStatus, showOnlyNotSuccessfullySynced}) {
  return (
    <div className="flex">
      {isConflicted && <Tooltip className="ml-1" title="Conflicted"><WarningAmberIcon color="warning" className="text-red" /></Tooltip>}
      {isContinueUntilTomorrow && <Tooltip className="ml-1" title="Timelog continues tomorrow"><RunningWithErrorsIcon color="warning" className="text-red" /></Tooltip>}
      {isJiraSyncingEnabled && <TimeLogJiraSyncStatusIcon className="ml-1" status={jiraSyncStatus} showOnlyNotSuccessfullySynced={showOnlyNotSuccessfullySynced} /> }
    </div>
  )
}