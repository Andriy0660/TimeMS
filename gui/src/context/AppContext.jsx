import {createContext, useEffect, useRef, useState} from "react";
import {toast, ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import dayjs from "dayjs";
import {startHourOfDay} from "../config/timeConfig.js";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import dateTimeService from "../service/dateTimeService.js";
import timeLogProcessingService from "../service/timeLogProcessingService.js";
import worklogApi from "../api/worklogApi.js";

const AppContext = createContext();

export const AppProvider = ({children}) => {
  const queryParams = new URLSearchParams(location.search);
  const [mode, setMode] = useState(queryParams.get("mode") || "Day");
  const [date, setDate] = useState(queryParams.get("date") ? dayjs(queryParams.get("date")) : dayjs())
  const [view, setView] = useState(queryParams.get("view") || "Day")

  const addAlert = ({type, text}) => {
    return toast[type](text);
  };

  const [timeLogs, setTimeLogs] = useState([]);
  const [hoveredTimeLogIds, setHoveredTimeLogIds] = useState([]);
  const [hoveredProgressIntervalId, setHoveredProgressIntervalId] = useState(0);
  const [hoveredConflictedIds, setHoveredConflictedIds] = useState([]);
  const [totalTimeLabel, setTotalTimeLabel] = useState("")

  const offset = startHourOfDay;
  const [groupByDescription, setGroupByDescription] = useState(!!queryParams.get("groupByDescription") || false);
  const [filterTickets, setFilterTickets] = useState([""])
  const [selectedTickets, setSelectedTickets] = useState([]);

  const queryClient = useQueryClient();

  const {
    data,
    isPending: isListing,
    error: listAllError,
    isPlaceholderData
  } = useQuery({
    queryKey: [timeLogApi.key, mode, date, offset],
    queryFn: () => {
      return timeLogApi.list({mode, date: dateTimeService.getFormattedDate(date)});
    },
    retryDelay: 300,
  });

  useEffect(() => {
    if (listAllError) {
      addAlert({
        text: `${listAllError.displayMessage} Try agail later`,
        type: "error"
      });
    }
  }, [listAllError]);

  const processedDataRef = useRef([]);
  useEffect(() => {
    const processedData = timeLogProcessingService.processData(data, selectedTickets);
    processedDataRef.current = processedData;

    const filterTickets = getFilterTickets(data);
    updateSelectedTicketsIfNeeded(filterTickets);

    const groupedData = groupAndSortData(processedData, groupByDescription);
    const label = calculateTotalTimeLabel(groupedData, groupByDescription);
    setTimeLogs(groupedData)
    setTotalTimeLabel(label);
  }, [data, groupByDescription, selectedTickets])

  function getFilterTickets(data) {
    const filterTickets = timeLogProcessingService.extractTickets(data);
    filterTickets.push("Without ticket");
    setFilterTickets(filterTickets);
    return filterTickets;
  }

  function updateSelectedTicketsIfNeeded(filterTickets) {
    const updatedTickets = selectedTickets.filter(ticket => filterTickets.includes(ticket));
    if (selectedTickets.toString() !== updatedTickets.toString()) {
      setSelectedTickets(updatedTickets);
    }
  }

  function groupAndSortData(data, groupByDescription) {
    if (groupByDescription) {
      return timeLogProcessingService.group(data, ["date", "ticketAndDescription"]);
    } else {
      return timeLogProcessingService.group(data, ["date"]);
    }
  }

  function calculateTotalTimeLabel(groupedData, groupByDescription) {
    if (groupByDescription) {
      return dateTimeService.formatDuration(dateTimeService.getTotalTimeGroupedByDateAndDescription(groupedData.data));
    } else {
      return dateTimeService.formatDuration(dateTimeService.getTotalTimeGroupedByDate(groupedData.data));
    }
  }

  const {mutateAsync: create} = useMutation({
    mutationFn: (body) => timeLogApi.create(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Time log is successfully created",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating time log failed:", error);
    }
  });

  const {mutateAsync: createWorklogFromTimeLog} = useMutation({
    mutationFn: (body) => worklogApi.create(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(worklogApi.key);
      addAlert({
        text: "Worklog is successfully created",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating worklog failed:", error);
    }
  });

  const {mutateAsync: divide} = useMutation({
    mutationFn: (id) => timeLogApi.divide(id),
    onSuccess: async () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Time logs is successfully divided",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Dividing time logs failed:", error);
    }
  });

  const {mutateAsync: importTimeLogs} = useMutation({
    mutationFn: (body) => timeLogApi.importTimeLogs(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Imported successfully",
        type: "success"
      });

    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Importing failed:", error);
    }
  });

  const {mutateAsync: update} = useMutation({
    mutationFn: (body) => timeLogApi.update(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Time log is successfully updated",
        type: "success"
      });
    },
    onError: async (error, body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Updating time log failed:", error);
    }
  });

  const {mutateAsync: deleteTimeLog} = useMutation({
    mutationFn: (id) => timeLogApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "You have successfully deleted time log",
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Deleting time log failed:", error);
    }
  });
  const {mutateAsync: setGroupDescription} = useMutation({
    mutationFn: (body) => timeLogApi.setGroupDescription(body),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "You have successfully set description",
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Setting group description failed:", error);
    }
  });
  const {mutateAsync: changeDate} = useMutation({
    mutationFn: (body) => timeLogApi.changeDate(body),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "You have successfully changed date",
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Changing date failed:", error);
    }
  });

  const {mutateAsync: syncWorklogsForIssue} = useMutation({
    mutationFn: (issueKey) => worklogApi.syncWorklogsForIssue(issueKey),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: `You have successfully synchronized worklogs for issue ${variables}`,
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("synchronizing worklogs for issue failed:", error);
    }
  });

  return (
    <>
      <ToastContainer
        position="bottom-left"
        autoClose={5000}
        limit={3}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick={false}
        rtl={false}
        pauseOnFocusLoss={false}
        draggable
        pauseOnHover={false}
        theme="light"
        transition: Bounce
      />
      <AppContext.Provider value={{
        date,
        setDate,
        view,
        setView,
        addAlert,
        mode,
        setMode,
        groupByDescription,
        setGroupByDescription,
        filterTickets,
        selectedTickets,
        setSelectedTickets,
        totalTimeLabel,
        timeLogs,
        processedDataRef,
        listAllError,
        isListing,
        isPlaceholderData,
        create,
        divide,
        importTimeLogs,
        update,
        createWorklogFromTimeLog,
        deleteTimeLog,
        setGroupDescription,
        changeDate,
        syncWorklogsForIssue,
        hoveredTimeLogIds,
        setHoveredTimeLogIds,
        hoveredProgressIntervalId,
        setHoveredProgressIntervalId,
        hoveredConflictedIds,
        setHoveredConflictedIds,
      }}>
        {children}
      </AppContext.Provider>
    </>
  );
};

export default AppContext;
