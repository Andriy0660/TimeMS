import {syncStatus} from "../../consts/syncStatus.js";
import {CircularProgress, IconButton, Tooltip} from "@mui/material";
import DoneIcon from "@mui/icons-material/Done.js";
import KeyboardDoubleArrowRightIcon from "@mui/icons-material/KeyboardDoubleArrowRight.js";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../../api/timeLogApi.js";
import syncUpworkApi from "../../api/syncUpworkApi.js";
import useAppContext from "../../context/useAppContext.js";
import dateTimeService from "../../service/dateTimeService.js";

export default function TimeLogUpworkSyncer({status, date, timeSpentSeconds}) {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();
  const {mutateAsync: sync, isPending} = useMutation({
    mutationFn: (body) => syncUpworkApi.sync(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Upwork: successfully synchronized",
        type: "success"
      });
    },
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Upwork: synchronizing failed:", error);
    }
  });


  return (
    <div className="ml-2 w-8 flex items-center">
      {status === syncStatus.SYNCED && (
          <Tooltip title="Upwork: Synchronized">
            <DoneIcon color="success" />
          </Tooltip>
        )
      }
      {status === syncStatus.NOT_SYNCED && (
        isPending ? <CircularProgress size={25} /> : <Tooltip title="Upwork: Synchronize">
          <IconButton
            onClick={() => sync({date: dateTimeService.getFormattedDate(date), timeSpentSeconds})}
            color="primary">
            <KeyboardDoubleArrowRightIcon />
          </IconButton>
        </Tooltip>
      )
      }

    </div>
  )
}