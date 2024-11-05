import {IconButton, LinearProgress, Tooltip} from "@mui/material";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined.js";
import ConfirmationModal from "../general/ConfirmationModal.jsx";
import {useEffect, useRef, useState} from "react";
import useAsyncCall from "../../hooks/useAsyncCall.js";
import dayjs from "dayjs";
import TimeLogNonEditableFields from "../timeLog/TimeLogNonEditableFields.jsx";
import timeLogService from "../../service/timeLogService.js";
import useAppContext from "../../context/useAppContext.js";
import {externalServiceIncludeDescription, isExternalServiceSyncingEnabled} from "../../config/config.js";
import {syncStatus} from "../../consts/syncStatus.js";
import Brightness1Icon from "@mui/icons-material/Brightness1";
import ExternalTimeLogConnectors from "./ExternalTimeLogConnectors.jsx";
import TimeLogSyncStatusIcon from "../timeLog/TimeLogSyncStatusIcon.jsx";

export default function ExternalTimeLog({externalTimeLog, onDelete, isExternalServiceEditMode}) {
  const externalTimeLogRef = useRef(null);
  const {externalTimeLogRefs, setExternalTimeLogRefs, timeLogRefs} = useAppContext();

  useEffect(() => {
    if (externalTimeLogRef.current && isExternalServiceEditMode) {
      setExternalTimeLogRefs((prev) => {
        const existingIndex = prev.findIndex(({externalTimeLog: {id}}) => id === externalTimeLog.id);
        if (existingIndex !== -1) {
          const updatedRefs = [...prev];
          updatedRefs[existingIndex] = {externalTimeLog, ref: externalTimeLogRef};
          return updatedRefs;
        } else {
          return [...prev, {externalTimeLog, ref: externalTimeLogRef}];
        }
      });
    }
  }, [externalTimeLogRef])

  const [isHovered, setIsHovered] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const startTime = dayjs(externalTimeLog.startTime, "HH:mm");
  const endTime = dayjs(externalTimeLog.endTime, "HH:mm");
  const isTimeLogInNextDay = timeLogService.getIsTimeLogInNextDayInfo(startTime, endTime);

  const {execute: handleDelete, isExecuting: isDeleteLoading} = useAsyncCall({
    fn: onDelete,
  })

  return (
    <div className="mb-2 px-4 py-1 shadow-md rounded-md bg-gray-50"
         ref={externalTimeLogRef}
         onMouseEnter={() => setIsHovered(true)}
         onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex justify-between">
        <div className="flex items-center pt-1.5 my-1">
          {isExternalServiceSyncingEnabled && isExternalServiceEditMode && externalServiceIncludeDescription && externalTimeLog.externalServiceSyncInfo.status !== syncStatus.NOT_SYNCED && (
            <>
              <Brightness1Icon className="mr-2" sx={{color: externalTimeLog.externalServiceSyncInfo.color}} />
              {isHovered && (
                <ExternalTimeLogConnectors
                  isHovered={isHovered}
                  sourceRefs={timeLogRefs}
                  targetRefs={externalTimeLogRefs}
                  sourceItem={externalTimeLog}
                />
              )}
            </>
          )}

          <TimeLogNonEditableFields
            startTime={startTime}
            endTime={endTime}
            isTimeLogInNextDay={isTimeLogInNextDay}
          />
          <TimeLogSyncStatusIcon serviceName="External Service" status={externalTimeLog.externalServiceSyncInfo.status} />
        </div>
        <div>
          {isHovered && (
            <Tooltip title="Delete">
              <IconButton
                className="mr-2 p-0"
                color="error"
                onClick={() => setShowDeleteModal(true)}
              >
                <DeleteOutlineOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )
          }
          <ConfirmationModal
            open={showDeleteModal}
            type="error"
            actionText="Delete"
            onConfirm={() => handleDelete(externalTimeLog.id)}
            onClose={() => {
              setShowDeleteModal(false);
              setIsHovered(false);
            }}
          >
            Are you sure you want to delete this timelog from external service?
          </ConfirmationModal>

        </div>
      </div>

      <div className="flex items-center">
        {externalTimeLog.description}
      </div>

      {isDeleteLoading && <LinearProgress />}
    </div>
  )
}