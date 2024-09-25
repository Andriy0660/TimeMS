import dateTimeService from "../service/dateTimeService.js";
import Duration from "./Duration.jsx";
import {Icon, IconButton, LinearProgress, Tooltip, Typography} from "@mui/material";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined.js";
import ConfirmationModal from "./ConfirmationModal.jsx";
import {useState} from "react";
import KeyboardDoubleArrowLeftIcon from '@mui/icons-material/KeyboardDoubleArrowLeft';
import useAsyncCall from "../hooks/useAsyncCall.js";
import DoneIcon from "@mui/icons-material/Done.js";
import SyncDisabledIcon from '@mui/icons-material/SyncDisabled';
import {TiArrowForward} from "react-icons/ti";
import dayjs from "dayjs";
import VerticalDivider from "./VerticalDivider.jsx";

export default function Worklog({worklog, onTimeLogCreate, onDelete, isJiraEditMode}) {
  const [isHovered, setIsHovered] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const startTime = dayjs(worklog.startTime, "HH:mm");
  const endTime = startTime.add(worklog.timeSpentSeconds, "second")
  const isTimeLogInNextDay = dateTimeService.isTimeLogInNextDay(startTime, endTime);

  const {execute: handleDeleteWorklog, isExecuting: isDeleteLoading} = useAsyncCall({
    fn: onDelete,
  })

  const {execute: handleCreateTimeLogFromWorklog, isExecuting: isCreateLoading} = useAsyncCall({
    fn: onTimeLogCreate,
  })
  
  return (
    <div className="mb-2 px-4 shadow-md rounded-md bg-gray-50"
         style={isJiraEditMode ? {backgroundColor: worklog.color} : {}}
         onMouseEnter={() => setIsHovered(true)}
         onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex justify-between">
        <div className="mt-1 flex items-center">
          <div className="flex mr-4 my-1">
            {isTimeLogInNextDay.startTime &&
              <Tooltip className="flex items-center mr-1" title="Next day">
                <Icon fontSize="small">
                  <TiArrowForward />
                </Icon>
              </Tooltip>
            }
            <Typography className="text-sm font-bold">
              {dateTimeService.getFormattedTime(startTime)}
            </Typography>
          </div>
          -
          <div className="flex mx-4 my-1">
            {isTimeLogInNextDay.endTime &&
              <Tooltip className="flex items-center mr-1" title="Next day">
                <Icon fontSize="small">
                  <TiArrowForward />
                </Icon>
              </Tooltip>
            }
            <Typography className={`text-sm font-bold`}>
              {dateTimeService.getFormattedTime(endTime)}
            </Typography>
          </div>
          <VerticalDivider />
          <div className="font-bold text-sm">
            {worklog.ticket}
          </div>
          <Duration duration={dateTimeService.formatDuration(worklog.timeSpentSeconds / 60)} />
          {worklog.synced
            ? (
              <Tooltip title="Synchronized">
                <DoneIcon color="success" />
              </Tooltip>
            )
            : (
              <Tooltip title="Not synchronized">
                <SyncDisabledIcon color="error" />
              </Tooltip>
            )
          }
        </div>
        <div>
          {isHovered && (
            <>
              {!worklog.synced && <Tooltip title="Save to my time logs">
                <IconButton
                  onClick={() => handleCreateTimeLogFromWorklog({
                    ticket: worklog.ticket,
                    date: worklog.date,
                    startTime: worklog.startTime,
                    description: worklog.comment,
                    timeSpentSeconds: worklog.timeSpentSeconds
                  })}
                  variant="outlined"
                  color="primary"
                  className="mr-2 p-0"
                >
                  <KeyboardDoubleArrowLeftIcon />
                </IconButton>
              </Tooltip>
              }
              <Tooltip title="Delete">
                <IconButton
                  className="mr-2 p-0"
                  color="error"
                  onClick={() => setShowDeleteModal(true)}
                >
                  <DeleteOutlineOutlinedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </>
          )
          }
          <ConfirmationModal
            open={showDeleteModal}
            type="error"
            actionText="Delete"
            onConfirm={() => handleDeleteWorklog({issueKey: worklog.ticket, id: worklog.id})}
            onClose={() => {
              setShowDeleteModal(false);
              setIsHovered(false);
            }}
          >
            Are you sure you want to delete this worklog?
          </ConfirmationModal>

        </div>
      </div>

      <div className="flex items-center">
        <div className="my-1">
          {worklog.comment}
        </div>
      </div>
      {isDeleteLoading || isCreateLoading && <LinearProgress />}
    </div>
  )
}