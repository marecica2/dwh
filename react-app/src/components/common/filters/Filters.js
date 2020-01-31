import React, { useCallback, useContext, useEffect, useState } from 'react';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import { MenuItem, Select, InputLabel, FormControl } from '@material-ui/core';
import chartApi from '../../../shared/api/chart.api';
import { AppContext } from '../../context/AppContext';

const titleCase = (val) => {
  const str = val.replace(/([A-Z]+)/g, ' $1').replace(/([A-Z][a-z])/g, ' $1');
  return str.charAt(0).toUpperCase() + str.slice(1);
};

const useStyles = makeStyles(theme => ({
  formControl: {
    marginRight: theme.spacing(2),
    marginBottom: theme.spacing(2),
    minWidth: 150,
  },
  selectEmpty: {
    marginTop: theme.spacing(2),
  },
}));

function getStyles(item, property, theme) {
  return {
    fontWeight:
      item != null && item[property] && item[property].indexOf(property) !== -1
        ? theme.typography.fontWeightMedium
        : theme.typography.fontWeightRegular,
  };
}

const MultiSelect = ({ dimension }) => {
  const classes = useStyles();
  const theme = useTheme();
  const [data, setData] = useState([]);
  const [values, setValues] = React.useState([]);
  const { dispatch } = useContext(AppContext);

  useEffect(() => {
    const api = async () => {
      const result = await chartApi().getDimensionValues({ dimension });
      setData(result);
    };
    api();
  }, [setData, dimension]);

  const handleChange = useCallback((event) => {
    dispatch({ type: 'filtersChange', value: {[dimension]: event.target.value}});
    setValues(event.target.value);
  }, [dispatch, dimension]);

  return (
    <FormControl className={classes.formControl}>
      <InputLabel id={`filter_${dimension}`}>{titleCase(dimension)}</InputLabel>
      <Select
        labelId={`filter_${dimension}`}
        name="mappingPreset"
        multiple
        value={values}
        onChange={handleChange}
      >
        {data.map(item => (
          <MenuItem
            key={item[dimension]}
            value={item[dimension]}
            style={getStyles(item, dimension, theme)}
          >
            {item[dimension]}
          </MenuItem>
))}
      </Select>
    </FormControl>
  )
};


export default () => {
  return (
    <div>
      <MultiSelect dimension="businessUnit" />
      <MultiSelect dimension="supplierName" />
      <MultiSelect dimension="standardServiceType" />
      <MultiSelect dimension="zone" />
    </div>
  )
}