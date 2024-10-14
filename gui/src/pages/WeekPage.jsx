import TableContainer from '@mui/material/TableContainer';
import dayjs from "dayjs";
import dateTimeService from "../service/dateTimeService.js";
import {startHourOfDay} from "../config/timeConfig.js";
import {useQuery} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {FormControlLabel, Switch} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import useViewChanger from "../hooks/useViewChanger.js";
import TimeLogList from "../components/TimeLogList.jsx";
import {useState} from "react";
import useTimeLogMutations from "../hooks/useTimeLogMutations.js";
import useProcessedTimeLogs from "../hooks/useProcessedTimeLogs.js";
import {GoTable} from "react-icons/go";
import ReorderIcon from '@mui/icons-material/Reorder';
import {weekViewMode} from "../consts/weekViewMode.js";
import ViewModeIcon from "../components/ViewModeIcon.jsx";
import {viewMode} from "../consts/viewMode.js";
import WeekTable from "../components/WeekTable.jsx";
import WeekJiraTable from "../components/WeekJiraTable.jsx";
import LoadingPage from "../components/LoadingPage.jsx";
import useSync from "../hooks/useSync.js";

export default function WeekPage() {
  const offset = startHourOfDay;

  const {changeView} = useViewChanger();
  const [view, setView] = useState(weekViewMode.TABLE);
  const {isJiraSyncingEnabled, date, setDate, addAlert, mode} = useAppContext();

  const {
    data: dayInfos,
    isPending
  } = useQuery({
    queryKey: [timeLogApi.key, "week", date, offset],
    queryFn: () => {
      return timeLogApi.getHoursForWeek({date: dateTimeService.getFormattedDate(date), includeTickets: isJiraSyncingEnabled});
    },
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Getting hours for week failed:", error);
    },
    retryDelay: 300,
  });

  const timeLogMutations = useTimeLogMutations();
  const syncMutations = useSync();

  const {
    groupByDescription, setGroupByDescription, timeLogs
  } = useProcessedTimeLogs();

  const handleClickDate = (date) => {
    setDate(dayjs(date))
    changeView(viewMode.DAY)
  }

  if (isPending) {
    return <LoadingPage />
  }

  return (
    <div className="w-3/5 mx-auto">
      <div className="flex justify-start my-2">
        <ViewModeIcon
          title={weekViewMode.TABLE}
          icon={<GoTable />}
          isActive={view === weekViewMode.TABLE}
          onClick={() => setView(weekViewMode.TABLE)}
        />
        <ViewModeIcon
          title={weekViewMode.LIST}
          icon={<ReorderIcon />}
          isActive={view === weekViewMode.LIST}
          onClick={() => setView(weekViewMode.LIST)}
        />
        {view === weekViewMode.LIST && <FormControlLabel
          control={
            <Switch
              checked={groupByDescription}
              onChange={(event) => setGroupByDescription((event.target.checked))}
            />
          }
          label="Group"
          labelPlacement="start"
          className="ml-10"
        />
        }
      </div>
      {view === weekViewMode.TABLE && <TableContainer className="flex mx-auto mb-3">
        {isJiraSyncingEnabled && (
          <WeekJiraTable dayInfos={dayInfos} handleClickDate={handleClickDate} />
        )}

        {!isJiraSyncingEnabled && (
          <WeekTable dayInfos={dayInfos} handleClickDate={handleClickDate}/>
        )}
      </TableContainer>
      }
      {view === weekViewMode.LIST && (
        <TimeLogList
          timeLogs={timeLogs}
          mode={mode}
          {...timeLogMutations}
          {...syncMutations}
        />
      )}
    </div>
  )
}