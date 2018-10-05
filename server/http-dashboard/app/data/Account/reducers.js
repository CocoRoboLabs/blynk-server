const initialState = {
  name: '',
  email: '',
  roleIds: 0,
  orgId: null,
  resetPasswordProcessing: false,
  credentials: {
    username: null,
    password: null,
  }
};

export default function Account(state = initialState, action) {
  switch (action.type) {

    case "API_ACCOUNT_SUCCESS":
      return {
        ...state,
        ...action.payload.data
      };

    case "API_LOGOUT_SUCCESS":
      return {
        ...state,
        credentials: {
          username: null,
          password: null,
        }
      };

    case "ACCOUNT_SAVE_CREDENTIALS":
      return {
        ...state,
        credentials: {
          username: action.value.username,
          password: action.value.password
        }
      };

    case "ACCOUNT_CLEAR_CREDENTIALS":
      return {
        ...state,
        credentials: {
          username: '',
          password: '',
        }
      };

    case "API_ACCOUNT_FAILURE":
      return {
        ...state,
      };

    case "API_ACCOUNT_SAVE_SUCCESS":
      return {
        ...state,
        ...action.payload.data
      };
    case "API_ACCOUNT_SAVE_FAILURE":
      return {
        ...state,
      };

    case "ACCOUNT_UPDATE_NAME":
      return {
        ...state,
        name: action.name
      };

    case "API_ACCOUNT_SEND_RESET_PASS":
      return {
        ...state,
        resetPasswordProcessing: true
      };

    case "API_ACCOUNT_SEND_RESET_PASS_SUCCESS":
      return {
        ...state,
        resetPasswordProcessing: false
      };

    case "API_ACCOUNT_SEND_RESET_PASS_FAILURE":
      return {
        ...state,
        resetPasswordProcessing: false
      };

    default:
      return state;
  }
}
