import dateTimeService from "../service/dateTimeService.js";
import Duration from "./Duration.jsx";
import {Icon, IconButton, LinearProgress, Tooltip, Typography} from "@mui/material";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined.js";
import ConfirmationModal from "./ConfirmationModal.jsx";
import {useEffect, useRef, useState} from "react";
import KeyboardDoubleArrowLeftIcon from '@mui/icons-material/KeyboardDoubleArrowLeft';
import useAsyncCall from "../hooks/useAsyncCall.js";
import {TiArrowForward} from "react-icons/ti";
import dayjs from "dayjs";
import VerticalDivider from "./VerticalDivider.jsx";
import useAppContext from "../context/useAppContext.js";
import Connector from "./Connector.jsx";
import Brightness1Icon from "@mui/icons-material/Brightness1.js";
import {syncStatus} from "../consts/syncStatus.js";
import TimeLogJiraSyncStatusIcon from "./TimeLogJiraSyncStatusIcon.jsx";

export default function Worklog({worklog, onTimeLogCreate, onDelete, isJiraEditMode}) {
  const worklogRef = useRef(null);
  const {isJiraSyncingEnabled, worklogRefs, setWorklogRefs, timeLogRefs} = useAppContext();

  useEffect(() => {
    if (worklogRef.current && isJiraEditMode) {
      setWorklogRefs((prev) => {
        const existingIndex = prev.findIndex(({worklog: {id}}) => id === worklog.id);
        if (existingIndex !== -1) {
          const updatedRefs = [...prev];
          updatedRefs[existingIndex] = {worklog, ref: worklogRef};
          return updatedRefs;
        } else {
          return [...prev, {worklog, ref: worklogRef}];
        }
      });
    }
  }, [worklogRef])

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
    <div className="mb-2 px-4 py-1 shadow-md rounded-md bg-gray-50"
         ref={worklogRef}
         onMouseEnter={() => setIsHovered(true)}
         onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex justify-between">
        <div className="flex items-center">
          {isJiraSyncingEnabled && isJiraEditMode && worklog.jiraSyncInfo.status !== syncStatus.NOT_SYNCED && (
            <>
              <Brightness1Icon className="mr-2" sx={{color: worklog.jiraSyncInfo.color}} />
              {isHovered && (
                <TimeLogWorklogConnectors
                  isHovered={isHovered}
                  sourceRefs={timeLogRefs}
                  targetRefs={worklogRefs}
                  sourceItem={worklog}
                />
              )}
            </>
          )}

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
          <Duration className="mx-2" duration={dateTimeService.formatDuration(worklog.timeSpentSeconds / 60)} />
          {isJiraSyncingEnabled && <TimeLogJiraSyncStatusIcon status={worklog.jiraSyncInfo.status}/> }
        </div>
        <div>
          {isHovered && (
            <>
              {worklog.jiraSyncInfo.status === syncStatus.NOT_SYNCED && <Tooltip title="Save to my time logs">
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
          {worklog.comment}
      </div>

      {isDeleteLoading || isCreateLoading && <LinearProgress />}
    </div>
  )
}