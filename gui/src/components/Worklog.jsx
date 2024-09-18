import dateTimeService from "../service/dateTimeService.js";
import Duration from "./Duration.jsx";
import {IconButton, LinearProgress, Tooltip} from "@mui/material";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined.js";
import ConfirmationModal from "./ConfirmationModal.jsx";
import {useState} from "react";
import KeyboardDoubleArrowLeftIcon from '@mui/icons-material/KeyboardDoubleArrowLeft';
import useAsyncCall from "../hooks/useAsyncCall.js";

export default function Worklog({worklog, onTimeLogCreate, onDelete}) {
  const [isHovered, setIsHovered] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const {execute: handleDeleteWorklog, isExecuting: isDeleteLoading} = useAsyncCall({
    fn: onDelete,
  })

  const {execute: handleCreateTimeLogFromWorklog, isExecuting: isCreateLoading} = useAsyncCall({
    fn: onTimeLogCreate,
  })

  return (
    <div className="mb-2 px-4 shadow-md rounded-md bg-gray-50"
         onMouseEnter={() => setIsHovered(true)}
         onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex justify-between">
        <div className="mt-1 flex items-center">
          <div className="font-bold text-sm">
            {worklog.ticket}
          </div>
          <Duration duration={dateTimeService.formatDuration(worklog.timeSpentSeconds / 60)} />
        </div>
        <div>
          {isHovered && (
            <>
              <Tooltip title="Save to my time logs">
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