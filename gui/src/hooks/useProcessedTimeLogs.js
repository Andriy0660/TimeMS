import {useEffect, useMemo, useState} from "react";
import {isExternalServiceSyncingEnabled, isJiraSyncingEnabled, startHourOfDay} from "../config/config.js";
import {useQuery} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import worklogApi from "../api/worklogApi.js";
import dateTimeService from "../service/dateTimeService.js";
import timeLogService from "../service/timeLogService.js";
import useAppContext from "../context/useAppContext.js";
import externalTimeLogApi from "../api/externalTimeLogApi.js";

export default function useProcessedTimeLogs() {
  const queryParams = new URLSearchParams(location.search);
  const {date, addAlert, mode} = useAppContext();
  const [timeLogs, setTimeLogs] = useState([]);
  const [totalTimeLabel, setTotalTimeLabel] = useState("");
  const [groupByDescription, setGroupByDescription] = useState(!!queryParams.get("groupByDescription") || false);

  const {
    data: timeLogsData,
    isPending: isListing,
    error: listAllError
  } = useQuery({
    queryKey: [timeLogApi.key, mode, date, startHourOfDay],
    queryFn: () => timeLogApi.list(dateTimeService.calculateDateRange(mode, date)),
    retryDelay: 300,
  });

  useEffect(() => {
    if (listAllError) {
      addAlert({
        text: `${listAllError.displayMessage} Try again later`,
        type: "error"
      });
    }
  }, [listAllError, addAlert]);

  const worklogsInfo = useProcessedWorklogs(isJiraSyncingEnabled, mode, date, timeLogsData);
  const externalTimeLogsInfo = useProcessedExternalTimeLogs(isExternalServiceSyncingEnabled, mode, date);

  const processedTimeLogsArray = useMemo(() => isJiraSyncingEnabled
      ? timeLogService.processData(timeLogsData, worklogsInfo.selectedTickets)
      : timeLogService.processData(timeLogsData),
    [timeLogsData, worklogsInfo.selectedTickets]);

  useEffect(() => {
    const groupedData = groupAndSortData(processedTimeLogsArray, groupByDescription);
    setTimeLogs(groupedData);
    setTotalTimeLabel(dateTimeService.formatMinutesToHM(
      timeLogService.getTotalMinutesForTimeLogsArray(processedTimeLogsArray)
    ))
  }, [timeLogsData, groupByDescription, isJiraSyncingEnabled, worklogsInfo.selectedTickets]);

  function groupAndSortData(data, groupByDescription) {
    if (groupByDescription) {
      return timeLogService.group(data, ["date", "ticketAndDescription"]);
    } else {
      return timeLogService.group(data, ["date"]);
    }
  }

  return {
    groupByDescription,
    setGroupByDescription,
    timeLogs,
    processedTimeLogsArray,
    isListing,
    totalTimeLabel,
    ...(isJiraSyncingEnabled ? worklogsInfo : {}),
    ...(isExternalServiceSyncingEnabled ? externalTimeLogsInfo : {}),
  };
}

function useProcessedWorklogs(isJiraSyncingEnabled, mode, date, timeLogsData) {
  const {addAlert} = useAppContext();
  const [filterTickets, setFilterTickets] = useState([""]);
  const [selectedTickets, setSelectedTickets] = useState([]);

  const {
    data: worklogs,
    isPending: isWorklogsListing,
    error: listWorklogsError,
  } = useQuery({
    queryKey: [worklogApi.key, mode, date, startHourOfDay],
    queryFn: () => worklogApi.list({mode, date: dateTimeService.getFormattedDate(date)}),
    enabled: isJiraSyncingEnabled,
    initialData: [],
    placeholderData: (prev) => prev,
    retryDelay: 300,
  });

  useEffect(() => {
    if (listWorklogsError) {
      addAlert({
        text: `${listWorklogsError.displayMessage} Try agail later`,
        type: "error"
      });
    }
  }, [listWorklogsError]);

  useEffect(() => {
    if (isJiraSyncingEnabled) {
      const newFilterTickets = timeLogService.extractTickets(timeLogsData);
      setFilterTickets(newFilterTickets);
      updateSelectedTicketsIfNeeded(newFilterTickets);
    }
  }, [isJiraSyncingEnabled, timeLogsData]);

  function updateSelectedTicketsIfNeeded(newFilterTickets) {
    const updatedTickets = selectedTickets.filter(ticket => newFilterTickets.includes(ticket));
    if (selectedTickets.toString() !== updatedTickets.toString()) {
      setSelectedTickets(updatedTickets);
    }
  }

  return {
    worklogs,
    isWorklogsListing,
    listWorklogsError,
    filterTickets,
    selectedTickets,
    setSelectedTickets,
  }
}

function useProcessedExternalTimeLogs(isExternalServiceSyncingEnabled, mode, date) {
  const {addAlert} = useAppContext();

  const {
    data: externalTimeLogs,
    isPending: isExternalTimeLogsListing,
    error: listExternalTimeLogsError
  } = useQuery({
    queryKey: [externalTimeLogApi.key, mode, date, startHourOfDay],
    queryFn: () => externalTimeLogApi.list({date: dateTimeService.getFormattedDate(date)}),
    retryDelay: 300,
  });

  useEffect(() => {
    if (listExternalTimeLogsError) {
      addAlert({
        text: `${listExternalTimeLogsError.displayMessage} Try agail later`,
        type: "error"
      });
    }
  }, [listExternalTimeLogsError]);

  return {
    externalTimeLogs,
    isExternalTimeLogsListing
  }
}