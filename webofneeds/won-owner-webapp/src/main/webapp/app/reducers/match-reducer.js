/**
 * Created by syim on 15.01.2016.
 */
import { actionTypes } from '../actions/actions';
import { repeatVar } from '../utils';
import Immutable from 'immutable';
import { createReducer } from 'redux-immutablejs'
import { combineReducersStable } from '../redux-utils';
import { buildCreateMessage } from '../won-message-utils';
import won from '../won-es6';

const initialState = Immutable.fromJS({
    isFetching: false,
    didInvalidate: false,
    matches: new Map()

})
export default createReducer(
    initialState,
    {
        [actionTypes.matches.hintsOfNeedRetrieved]:(state,action)=>{
            return state.setIn(['matches',action.payload.ownNeed],Immutable.fromJS(action.payload.connections))
        }
    }

)