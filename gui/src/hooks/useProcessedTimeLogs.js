import {useEffect, useState} from "react";
import {isJiraSyncingEnabled, startHourOfDay} from "../config/config.js";
import {useQuery} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import worklogApi from "../api/worklogApi.js";
import dateTimeService from "../service/dateTimeService.js";
import timeLogService from "../service/timeLogService.js";
import useAppContext from "../context/useAppContext.js";

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

  const jiraInfo = useJira(isJiraSyncingEnabled, mode, date);

  const processedTimeLogsArray = timeLogService.processData(timeLogsData);
  useEffect(() => {
    let processedData = timeLogService.processData(timeLogsData);

    if (isJiraSyncingEnabled) {
      processedData = timeLogService.processData(timeLogsData, jiraInfo.selectedTickets);
    }

    const groupedData = groupAndSortData(processedData, groupByDescription);
    setTimeLogs(groupedData);
    setTotalTimeLabel(timeLogService.getTotalTimeLabel(groupedData, groupByDescription));
  }, [timeLogsData, groupByDescription, isJiraSyncingEnabled, jiraInfo.selectedTickets]);

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
    ...(isJiraSyncingEnabled ? jiraInfo : {}),
  };
}

function useJira(isJiraSyncingEnabled, mode, date) {
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
      const newFilterTickets = timeLogService.extractTickets(worklogs);
      setFilterTickets(newFilterTickets);
      updateSelectedTicketsIfNeeded(newFilterTickets);
    }
  }, [isJiraSyncingEnabled, worklogs]);

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