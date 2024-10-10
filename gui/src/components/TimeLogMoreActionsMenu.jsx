import {ListItemIcon, ListItemText, Menu, MenuItem, Typography} from "@mui/material";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined.js";
import SplitscreenIcon from "@mui/icons-material/Splitscreen.js";
import TimeLogJiraSyncButtons from "./TimeLogJiraSyncButtons.jsx";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined.js";
import KeyboardTabOutlinedIcon from "@mui/icons-material/KeyboardTabOutlined.js";
import dateTimeService from "../service/dateTimeService.js";
import StopCircleOutlinedIcon from "@mui/icons-material/StopCircleOutlined.js";
import dayjs from "dayjs";
import StartOutlinedIcon from "@mui/icons-material/StartOutlined.js";
import ArrowForwardIosIcon from "@mui/icons-material/ArrowForwardIos";
import ArrowBackIosIcon from "@mui/icons-material/ArrowBackIos";
import useAppContext from "../context/useAppContext.js";

export default function TimeLogMoreActionsMenu({
  moreActionsMenuEl,
  handleCloseMoreActionsMenu,
  timeLog,
  setIsEditing,
  isContinueUntilTomorrow,
  handleCreateTimeLog,
  handleUpdateTimeLog,
  setShowDeleteModal,
  handleDivideTimeLog,
  handleChangeDate,

  handleCreateWorklog,
  handleSyncForTicket,
  handleSyncFromJira,
  handleSyncIntoJira
}) {
  const {isJiraSyncingEnabled} = useAppContext();
  const currentTime = dayjs();

  const statusConfig = {
    Done: {
      icon: <KeyboardTabOutlinedIcon color="primary" fontSize="small" />,
      onClick: () => handleCreateTimeLog({
        ticket: timeLog.ticket,
        startTime: dateTimeService.getFormattedDateTime(currentTime),
        description: timeLog.description
      }),
      text: "Continue"
    },
    InProgress: {
      icon: <StopCircleOutlinedIcon color="warning" fontSize="small" />,
      onClick: () => handleUpdateTimeLog({
        id: timeLog.id,
        ticket: timeLog.ticket,
        startTime: timeLog.startTime,
        endTime: currentTime,
      }),
      text: "Stop"
    },
    Pending: {
      icon: <StartOutlinedIcon color="primary" fontSize="small" />,
      onClick: () => handleUpdateTimeLog({
        id: timeLog.id,
        ticket: timeLog.ticket,
        startTime: currentTime,
      }),
      text: "Start"
    }
  };

  const currentAction = statusConfig[timeLog.status];

  return (
    <Menu
      anchorEl={moreActionsMenuEl}
      open={!!moreActionsMenuEl}
      onClose={() => handleCloseMoreActionsMenu()}
    >
      <MenuItem onClick={() => setIsEditing(true)}>
        <ListItemIcon>
          <EditOutlinedIcon color="success" fontSize="small" />
        </ListItemIcon>
        <ListItemText>
          <Typography className="text-sm">Edit</Typography>
        </ListItemText>
      </MenuItem>

      {statusConfig[timeLog.status] && (
        <MenuItem onClick={currentAction.onClick}>
          <ListItemIcon>
            {currentAction.icon}
          </ListItemIcon>
          <ListItemText>
            <Typography className="text-sm">{currentAction.text}</Typography>
          </ListItemText>
        </MenuItem>
      )}
      {isContinueUntilTomorrow &&
        <MenuItem onClick={() => handleDivideTimeLog(timeLog.id)}>
          <ListItemIcon>
            <SplitscreenIcon color="primary" fontSize="small" />
          </ListItemIcon>
          <ListItemText>
            <Typography className="text-sm">Divide into two days</Typography>
          </ListItemText>
        </MenuItem>
      }
      {isJiraSyncingEnabled && <TimeLogJiraSyncButtons
        timeLog={timeLog}
        handleCreateWorklog={handleCreateWorklog}
        handleSyncForTicket={handleSyncForTicket}
        handleSyncFromJira={handleSyncFromJira}
        handleSyncIntoJira={handleSyncIntoJira} />
      }

      <MenuItem onClick={() => {
        handleChangeDate({id: timeLog.id, isNext: false})
      }}>
        <ListItemIcon>
          <ArrowBackIosIcon color="primary" fontSize="small" />
        </ListItemIcon>
        <ListItemText>
          <Typography className="text-sm">To previous day</Typography>
        </ListItemText>
      </MenuItem>

      <MenuItem onClick={() => {
        handleChangeDate({id: timeLog.id, isNext: true})
      }}>
        <ListItemIcon>
          <ArrowForwardIosIcon color="primary" fontSize="small" />
        </ListItemIcon>
        <ListItemText>
          <Typography className="text-sm">To next day</Typography>
        </ListItemText>
      </MenuItem>

      <MenuItem onClick={() => setShowDeleteModal(true)}>
        <ListItemIcon>
          <DeleteOutlineOutlinedIcon color="error" fontSize="small" />
        </ListItemIcon>
        <ListItemText>
          <Typography className="text-sm">Delete</Typography>
        </ListItemText>
      </MenuItem>


    </Menu>
  )
}