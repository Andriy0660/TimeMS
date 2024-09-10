import PendingIcon from '@mui/icons-material/Pending';
import {Tooltip} from "@mui/material";
export default function MonthPageDuration({handleClickDate, title, isInProgress}) {
  return (
    <div
      onClick={handleClickDate}
      className="flex justify-center items-center w-full bg-transparent text-black text-lg font-medium hover:bg-blue-50 hover:cursor-pointer"
    >
      {isInProgress && <Tooltip title="In Progress"><PendingIcon color="primary" className="mr-1" /></Tooltip>}
      {title !== "0h 0m" ? title : ""}
    </div>
  )
}