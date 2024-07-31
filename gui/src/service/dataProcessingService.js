const dataProcessingService = {
  group(data, groupOrder) {
    let res = data;
    groupOrder.forEach(groupBy => {
      res = this.groupBy(res, groupBy);
    })
    return {
      data: res,
      groupOrder
    };
  },
  groupBy(data, key) {
    return this.groupNested(data, key);
  },
  groupNested(data, key) {
    if (!Array.isArray(data)) {
      Object.keys(data).forEach(groupKey => {
        data[groupKey] = this.groupNested(data[groupKey], key);
      });
      return data;
    } else {
      return this.groupList(data, key);
    }
  },
  groupList(data, key) {
    return data.reduce((result, item) => {
      const groupKey = item[key];
      if (!result[groupKey]) {
        result[groupKey] = []
      }
      result[groupKey].push(item);
      return result;
    }, {});
  },
};

export default dataProcessingService;