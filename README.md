# CanceRater

CanceRater aims to predict cancer probability of a patient. Akka-Http and Spark ML (MLLib) are the main components.

Logistic Regression algorithm is used to predict the cancer probability (in percentage) of a given patient from the pre-learned model (from a given corpus).

Logistic regression is used because of its simplicity and performance for such a task. Support Vector Machines are the other option for such task but SVMs are adviced to be used when the feature set is too large compared to sample size.

Decision Trees (e.g. Random Forest) can be used if the probability percentage is not the aim and only the prediction of the system is enough. Therefore, we eliminate that option because of our problem definition.

The pipeline approach provided by the Spark ML library is used. Categorical features are Gender and Job fields. Gender conversion is done at the enumeration level, Job conversion is done with the StringIndexer. After all the features are converted to numeric values, StandardScaler is applied to scale features to be able to improve the performance of the regression. 10-fold Cross Validation (0.8 training, 0.2 test split) is applied to find best model, since test corpus is too small.

The project is an sbt project, so you can run the system basically with sbt commands on the root directory after clone operation:

  ```sh
    > sbt
    > clean
    > compile
    > test
    > run
  ```

The rest layer of the system provides 2 endpoint:
 * GET /cancerater/cm => returns the confusion matrix of the current model on test data.
 * POST /cancerater/check => takes a patient info json and return its cancer probability.
   * input:

        ```json
        {
            "gender": "Male",
            "age": 18,
            "weight": 70,
            "height": 180,
            "job": "Student"
        }
        ```

    * output:
      ```json
      {
          "score": 0.12
      }
      ```

Here is some reference pages to read:

 * [feature scaling](http://sebastianraschka.com/Articles/2014_about_feature_scaling.html)

 * [LR over Decision Trees](https://www.quora.com/What-are-the-advantages-of-logistic-regression-over-decision-trees)

 * [Advantages of different classification algorithms](https://www.quora.com/What-are-the-advantages-of-different-classification-algorithms)

 * [LR vs DT vs SVMs](http://www.edvancer.in/logistic-regression-vs-decision-trees-vs-svm-part2/)

 * [Prediction with spark mllib](http://blog.cloudera.com/blog/2016/02/how-to-predict-telco-churn-with-apache-Spark-MLLib/)


