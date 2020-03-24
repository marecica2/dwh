import { portalApiPath } from '../../settings';

const getAppStateUrl = (tenant, project) => `${portalApiPath}/portal/status?tenant=${tenant}&projectId=${project}`;

export default {
  getAppStateUrl,
};
