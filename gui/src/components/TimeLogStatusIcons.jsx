import {Tooltip} from "@mui/material";
import WarningAmberIcon from "@mui/icons-material/WarningAmber.js";
import TimeLogJiraSyncStatusIcon from "./TimeLogJiraSyncStatusIcon.jsx";
import useAppContext from "../context/useAppContext.js";
import RunningWithErrorsIcon from "@mui/icons-material/RunningWithErrors.js";

export default function TimeLogStatusIcons({isConflicted, isContinueUntilTomorrow, jiraSyncStatus, showOnlyNotSuccessfullySynced}) {
  const {isJiraSyncingEnabled} = useAppContext();
  return (
    <>
      {isConflicted && <Tooltip className="ml-1" title="Conflicted"><WarningAmberIcon color="warning" className="text-red" /></Tooltip>}
      {isContinueUntilTomorrow && <Tooltip className="ml-1" title="Timelog continues tomorrow"><RunningWithErrorsIcon color="warning" className="text-red" /></Tooltip>}
      {isJiraSyncingEnabled && <TimeLogJiraSyncStatusIcon className="ml-1" status={jiraSyncStatus} showOnlyNotSuccessfullySynced={showOnlyNotSuccessfullySynced} /> }
    </>
  )
}